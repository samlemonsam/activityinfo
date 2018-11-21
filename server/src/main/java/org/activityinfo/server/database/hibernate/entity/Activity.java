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

import org.activityinfo.legacy.shared.model.Published;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * After the
 * {@link Database}, the
 * activity is the second level of organization in ActivityInfo. Each activity
 * has its set of indicators and attributes.
 * <p/>
 * Realized activities takes place at
 * {@link org.activityinfo.server.database.hibernate.entity.Site} sites.
 *
 * @author Alex Bertram
 */
@Entity
@NamedQuery(name = "queryMaxSortOrder", query = "select max(e.sortOrder) from Activity e where e.database.id = ?1")
public class Activity implements Serializable, Deleteable, Orderable, HasJson {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private int id;
    private LocationType locationType;

    private Database database;
    private Folder folder;
    private String name;
    private String category;

    private int reportingFrequency;

    private boolean allowEdit;
    private int sortOrder;

    private Date dateDeleted;

    private Set<Indicator> indicators = new HashSet<>(0);

    private Set<Site> sites = new HashSet<>(0);
    private Set<AttributeGroup> attributeGroups = new HashSet<>(0);
    private Set<LockedPeriod> lockedPeriods = new HashSet<>();

    private String mapIcon;

    private int published = Published.NOT_PUBLISHED.getIndex();

    private long version;
    private long siteVersion;
    private long schemaVersion;

    private boolean classicView = true;

    private String formClassJson;
    private byte[] gzFormClassJson;

    public Activity() {
        this.version = 1;
        this.schemaVersion = 1;
        this.siteVersion = 1;
    }

    public Activity(Activity sourceActivity) {
        this.locationType = sourceActivity.getLocationType();

        this.database = sourceActivity.getDatabase();
        this.name = sourceActivity.getName();
        this.category = sourceActivity.getCategory();

        this.reportingFrequency = sourceActivity.reportingFrequency;
        this.allowEdit = sourceActivity.allowEdit;
        this.sortOrder = sourceActivity.sortOrder;
        this.dateDeleted = sourceActivity.dateDeleted;

        this.mapIcon = sourceActivity.mapIcon;
        this.published = sourceActivity.published;
        this.classicView = sourceActivity.classicView;
    }

    @Id
    @Offline
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ActivityId", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Transient
    public ResourceId getFormId() {
        return CuidAdapter.activityFormClass(id);
    }

    @Transient
    public ResourceId getParentResourceId() {
        if(folder == null) {
            return CuidAdapter.databaseId(database.getId());
        } else {
            return CuidAdapter.folderId(folder.getId());
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @Offline
    @JoinColumn(name = "LocationTypeId", nullable = false)
    public LocationType getLocationType() {
        return this.locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    @Offline
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FolderId", nullable = true)
    public Folder getFolder() {
        return this.folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
        if(this.folder == null) {
            this.category = null;
        } else {
            this.category = folder.getName();
        }
    }

    @Offline
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatabaseId", nullable = false)
    public Database getDatabase() {
        return this.database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Offline
    @Column(name = "Name", nullable = false, length = 255)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "classicView", nullable = false)
    public boolean isClassicView() {
        return classicView;
    }

    public void setClassicView(boolean classicView) {
        this.classicView = classicView;
    }

    @Offline
    @Column(name = "ReportingFrequency", nullable = false)
    public int getReportingFrequency() {
        return this.reportingFrequency;
    }

    public void setReportingFrequency(int reportingFrequency) {
        this.reportingFrequency = reportingFrequency;
    }

    @Column(name = "AllowEdit", nullable = false)
    public boolean isAllowEdit() {
        return this.allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    @Offline
    @Override
    @Column(name = "SortOrder", nullable = false)
    public int getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinTable(name = "AttributeGroupInActivity",
            joinColumns = {@JoinColumn(name = "ActivityId", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "AttributeGroupId", nullable = false, updatable = false)})
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    public Set<AttributeGroup> getAttributeGroups() {
        return this.attributeGroups;
    }

    public void setAttributeGroups(Set<AttributeGroup> attributeGroups) {
        this.attributeGroups = attributeGroups;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "activity")
    @org.hibernate.annotations.OrderBy(clause = "sortOrder")
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    public Set<Indicator> getIndicators() {
        return this.indicators;
    }

    public void setIndicators(Set<Indicator> indicators) {
        this.indicators = indicators;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "activity")
    public Set<Site> getSites() {
        return this.sites;
    }

    public void setSites(Set<Site> sites) {
        this.sites = sites;
    }

    /**
     *
     * @return the FormClass resource encoded as JSON
     */
    @Lob
    @Column(name = "formClass")
    @Offline(sync = false)
    public String getJson() {
        return formClassJson;
    }

    public void setJson(String formClassJson) {
        this.formClassJson = formClassJson;
    }

    @Column(name = "gzFormClass")
    @Offline(sync = false)
    public byte[] getGzJson() {
        return gzFormClassJson;
    }

    public void setGzJson(byte[] gzFormClassJson) {
        this.gzFormClassJson = gzFormClassJson;
    }

    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getDateDeleted() {
        return this.dateDeleted;
    }

    public void setDateDeleted(Date date) {
        this.dateDeleted = date;
    }

    @Override
    @Offline(sync = false)
    public void delete() {
        setDateDeleted(new Date());
        getDatabase().setLastSchemaUpdate(new Date());
    }

    @Offline
    @Column(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    @Transient
    public boolean isDeleted() {
        return getDateDeleted() != null;
    }

    @Column(length = 255, nullable = true)
    public String getMapIcon() {
        return mapIcon;
    }

    public void setMapIcon(String mapIcon) {
        this.mapIcon = mapIcon;
    }

    public void setLockedPeriods(Set<LockedPeriod> lockedPeriods) {
        this.lockedPeriods = lockedPeriods;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "activity")
    @BatchSize(size = DEFAULT_BATCH_SIZE)
    public Set<LockedPeriod> getLockedPeriods() {
        return lockedPeriods;
    }

    @Offline
    @Column(name = "published")
    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public long getSiteVersion() {
        return siteVersion;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSiteVersion(long siteVersion) {
        this.siteVersion = siteVersion;
    }

    @Column
    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public long incrementSchemaVersion() {
        version++;
        schemaVersion = version;
        return version;
    }

    public long incrementSiteVersion() {
        version++;
        siteVersion = version;
        return version;
    }

    public Resource asResource() {
        return new Resource.Builder()
            .setId(getFormId())
            .setType(ResourceType.FORM)
            .setParentId(getParentResourceId())
            .setLabel(getName())
            .setPublic(getPublished() != Published.NOT_PUBLISHED.getIndex())
            .build();
    }

}
