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

import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alex Bertram
 */
@Entity @JsonAutoDetect(JsonMethod.NONE)
public class LocationType implements Serializable, Deleteable {

    private int id;
    private boolean reuse;
    private String name;
    private Country country;
    private Set<Location> locations = new HashSet<Location>(0);
    private Set<Activity> activities = new HashSet<Activity>(0);
    private String workflowId;
    private long version;
    private long locationVersion;

    private UserDatabase database;

    private AdminLevel boundAdminLevel;
    private Date dateDeleted;

    public LocationType() {
    }

    @Id
    @JsonProperty
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LocationTypeId", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "Reuse", nullable = false)
    public boolean isReuse() {
        return this.reuse;
    }

    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }

    @JsonProperty @Column(name = "Name", nullable = false, length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "CountryId", nullable = false)
    public Country getCountry() {
        return this.country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "DatabaseId", nullable = true)
    public UserDatabase getDatabase() {
        return this.database;
    }

    public void setDatabase(UserDatabase database) {
        this.database = database;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "locationType")
    public Set<Location> getLocations() {
        return this.locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "locationType")
    public Set<Activity> getActivities() {
        return this.activities;
    }

    public void setActivities(Set<Activity> activities) {
        this.activities = activities;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "BoundAdminLevelId", nullable = true)
    public AdminLevel getBoundAdminLevel() {
        return boundAdminLevel;
    }

    public void setBoundAdminLevel(AdminLevel boundAdminLevel) {
        this.boundAdminLevel = boundAdminLevel;
    }

    /**
     * @return the id of the workflow associated with this LocationType.
     */
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @Column @Temporal(value = TemporalType.TIMESTAMP)
    public Date getDateDeleted() {
        return this.dateDeleted;
    }

    public void setDateDeleted(Date date) {
        this.dateDeleted = date;
    }

    @Override
    public void delete() {
        setDateDeleted(new Date());
    }

    @Override @Transient
    public boolean isDeleted() {
        return getDateDeleted() != null;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getLocationVersion() {
        return locationVersion;
    }

    public void setLocationVersion(long locationVersion) {
        this.locationVersion = locationVersion;
    }

    public long incrementVersion() {
        version++;
        return version;
    }

    public static LocationType queryNullLocationType(EntityManager em, Activity activity) {
        return queryNullLocationType(em, activity.getDatabase().getCountry().getId());
    }

    public static LocationType queryNullLocationType(EntityManager em, int countryId) {
        
        if(countryId == LocationTypeDTO.GLOBAL_COUNTRY_ID) {
            return em.getReference(LocationType.class, LocationTypeDTO.GLOBAL_NULL_LOCATION_TYPE);
                    
        } else {

            List<LocationType> nullTypes = em.createQuery("select t from LocationType t " +
                            "where t.country.id = :countryId and t.name = 'Country'" +
                            " and t.database is null",
                    LocationType.class)
                    .setParameter("countryId", countryId)
                    .getResultList();
            if (nullTypes.isEmpty()) {
                throw new IllegalStateException("Cannot find null location type for country " + countryId);
            } else if (nullTypes.size() > 1) {
                throw new IllegalStateException("Found multiple null location type for country " + countryId);
            } else {
                return nullTypes.get(0);
            }
        }
    }
}
