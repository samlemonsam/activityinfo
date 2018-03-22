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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.RecordLockBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateInterval;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class LockedPeriod implements Serializable, HardDeleteable {

    private Date fromDate;
    private Date toDate;
    private String name;
    private int id;
    private Database database;
    private Database userDatabase;
    private Project project;
    private Activity activity;
    private boolean enabled;

    public LockedPeriod() {
    }

    public LockedPeriod(LockedPeriod lockedPeriod) {
        this.fromDate = lockedPeriod.fromDate;
        this.toDate = lockedPeriod.toDate;
        this.name = lockedPeriod.name;
        this.database = lockedPeriod.database;
        this.project = lockedPeriod.project;
        this.activity = lockedPeriod.activity;
        this.enabled = lockedPeriod.enabled;
    }

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate.atMidnightInMyTimezone();
    }

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate.atMidnightInMyTimezone();
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LockedPeriodId", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "databaseId", nullable = false)
    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Deprecated
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserDatabaseId", nullable = true)
    public Database getUserDatabase() {
        return userDatabase;
    }

    @Deprecated
    public void setUserDatabase(Database userDatabase) {
        this.userDatabase = userDatabase;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ProjectId", nullable = true)
    public Project getProject() {
        return project;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ActivityId", nullable = true)
    public Activity getActivity() {
        return activity;
    }

    public void setEnabled(boolean isEnabled) {
        this.enabled = isEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the id of the resource to which this lock applies.
     */
    @Transient
    public ResourceId getResourceId() {
        if(activity != null) {
            return CuidAdapter.activityFormClass(activity.getId());
        } else {
            return CuidAdapter.databaseId(database.getId());
        }
    }

    @Transient
    public LocalDateInterval getDateRange() {
        return new LocalDateInterval(new org.activityinfo.model.type.time.LocalDate(fromDate), new org.activityinfo.model.type.time.LocalDate(toDate));
    }

    @Transient
    public Database getParentDatabase() {
        return database;
    }

    public RecordLock asDatabaseLock() {
        return new RecordLockBuilder()
            .setId(CuidAdapter.lockId(getId()))
            .setResourceId(getResourceId())
            .setDateRange(getDateRange())
            .setLabel(getName())
            .build();

    }

    @Override
    public void delete() {
        getParentDatabase().updateVersion();
        if (activity != null) {
            activity.getLockedPeriods().remove(this);
        }
        if (database != null) {
            database.getLockedPeriods().remove(this);
        }
        if (project != null) {
            project.getLockedPeriods().remove(this);
        }
        this.activity = null;
        this.database = null;
        this.project = null;
    }

}
