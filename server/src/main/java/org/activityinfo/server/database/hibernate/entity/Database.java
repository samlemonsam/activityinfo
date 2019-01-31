/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.database.hibernate.entity;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The UserDatabase is the broadest unit of organization within ActivityInfo.
 * Individual databases each has an owner who controls completely the
 * activities, indicators, partner organizations and the rights of other users
 * to view, edit, and design the database.
 *
 * @author Alex Bertram
 */
@Entity
@Table(name = "userdatabase")
public class Database implements java.io.Serializable, Deleteable {

    private static final long serialVersionUID = 7405094318163898712L;

    private static final int DEFAULT_BATCH_SIZE = 100;

    private int id;
    private Country country;
    private Date startDate;
    private String fullName;
    private String name;
    private User owner;
    private Set<Partner> partners = new HashSet<>(0);
    private Set<Activity> activities = new HashSet<>(0);
    private Set<UserPermission> userPermissions = new HashSet<>(0);
    private Set<Project> projects = new HashSet<>(0);
    private Set<LockedPeriod> lockedPeriods = new HashSet<>(0);
    private Set<Target> targets = new HashSet<>(0);
    private Date dateDeleted;

    private long version;
    private long metaVersion;

    private String transferToken;
    private User transferUser;
    private Date transferRequestDate;

    public Database() {
    }

    public Database(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO) 
    @Column(name = "DatabaseId", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Transient
    public ResourceId getResourceId() {
        return CuidAdapter.databaseId(getId());
    }

    /**
     * At present, each database can contain data on activities that take place
     * in one and only one country.
     *
     * @return The country assocatited with this database.
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "CountryId", nullable = false)
    public Country getCountry() {
        return this.country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * @return The date on which the activities defined by this database
     * started. I.e. provides a minimum bound for the dates of
     * activities.
     */
    @Temporal(TemporalType.DATE) 
    @Column(name = "StartDate", length = 23)
    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return The full name of the database
     */
    @Column(name = "FullName", length = 50)
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return The short name of the database (generally an acronym)
     */
    @Column(name = "Name", length = 255, nullable = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The user who owns this database
     */
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "OwnerUserId", nullable = false)
    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * @return The list of partner organizations involved in this database.
     * (Partner organizations can own activity sites)
     */
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinTable(name = "PartnerInDatabase",
            joinColumns = {@JoinColumn(name = "DatabaseId", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "PartnerId", nullable = false, updatable = false)})
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<Partner> getPartners() {
        return this.partners;
    }

    public void setPartners(Set<Partner> partners) {
        this.partners = partners;
    }

    /**
     * @return The list of activities followed by this database
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "database")
    @org.hibernate.annotations.OrderBy(clause = "sortOrder")
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<Activity> getActivities() {
        return this.activities;
    }

    public void setActivities(Set<Activity> activities) {
        this.activities = activities;
    }

    /**
     * @return The list of users who have access to this database and their
     * respective permissions. (Read, write, read all partners)
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "database")
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<UserPermission> getUserPermissions() {
        return this.userPermissions;
    }

    public void setUserPermissions(Set<UserPermission> userPermissions) {
        this.userPermissions = userPermissions;
    }

    /**
     * @return The date on which this database was deleted by the user, or null
     * if this database is not deleted.
     */
    @Column @Temporal(value = TemporalType.TIMESTAMP)
    public Date getDateDeleted() {
        return this.dateDeleted;
    }

    protected void setDateDeleted(Date date) {
        this.dateDeleted = date;
    }

    /**
     * Marks this database as deleted. (Though the row is not removed from the
     * database)
     */
    @Override
    public void delete() {
        Date now = new Date();
        setDateDeleted(now);
        setLastSchemaUpdate(now);
    }

    /**
     * @return True if this database was deleted by its owner.
     */
    @Override @Transient
    public boolean isDeleted() {
        return getDateDeleted() != null;
    }

    /**
     * Gets the timestamp on which structure of the database (activities,
     * indicators, etc) was last modified.
     *
     * @return The timestamp on which the structure of the database was last
     * modified.
     */
    @Transient
    public Date getLastSchemaUpdate() {
        return new Date(version);
    }

    @Offline(sync = false)
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
    
    @Offline(sync = false)
    public long getMetaVersion() {
        return metaVersion;
    }

    public void setMetaVersion(long metaVersion) {
        this.metaVersion = metaVersion;
    }

    /**
     * Sets the timestamp on which the structure of the database (activities,
     * indicateurs, etc was last modified, <b>excluding</b> changes which affect the
     * {@link org.activityinfo.model.database.DatabaseMeta}
     *
     * @param lastSchemaUpdate
     */
    public void setLastSchemaUpdate(Date lastSchemaUpdate) {
        setVersion(lastSchemaUpdate.getTime());
    }

    /**
     * Sets the timestamp on which the structure of the database (activities,
     * indicateurs, etc was last modified, <b>including</b> changes which affect the
     * {@link org.activityinfo.model.database.DatabaseMeta}
     *
     * @param lastSchemaUpdate
     */
    public void setLastMetaSchemaUpdate(Date lastSchemaUpdate) {
        setVersion(lastSchemaUpdate.getTime());
        setMetaVersion(lastSchemaUpdate.getTime());
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "database")
    public Set<Project> getProjects() {
        return projects;
    }

    public void setLockedPeriods(Set<LockedPeriod> lockedPeriods) {
        this.lockedPeriods = lockedPeriods;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "database")
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<LockedPeriod> getLockedPeriods() {
        return lockedPeriods;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "database")
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<Target> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }

    public boolean hasPendingTransfer() {
        return getTransferToken() != null;
    }

    /**
     * @return The pending transfer token on this database (if any)
     */
    @Column(name = "TransferToken", unique = true, length = 34)
    @Offline(sync = false)
    public String getTransferToken() {
        return transferToken;
    }

    public void setTransferToken(String transferToken) {
        this.transferToken = transferToken;
    }

    /**
     * @return The user who owns this database
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TransferUser")
    @Offline(sync = false)
    public User getTransferUser() {
        return this.transferUser;
    }

    public void setTransferUser(User transferUser) {
        this.transferUser = transferUser;
    }

    @Column(name = "TransferTokenIssueDate")
    @Offline(sync = false)
    public Date getTransferRequestDate() {
        return transferRequestDate;
    }

    public void setTransferRequestDate(Date transferRequestDate) {
        this.transferRequestDate = transferRequestDate;
    }
}
