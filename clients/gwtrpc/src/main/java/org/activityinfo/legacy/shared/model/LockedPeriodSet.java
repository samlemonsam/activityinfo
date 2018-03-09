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
package org.activityinfo.legacy.shared.model;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Collection;
import java.util.Date;

public class LockedPeriodSet {

    private Multimap<Integer, LockedPeriodDTO> activityLocks = HashMultimap.create();
    private Multimap<Integer, LockedPeriodDTO> projectLocks = HashMultimap.create();

    public LockedPeriodSet(SchemaDTO schema) {
        for (UserDatabaseDTO db : schema.getDatabases()) {
            indexLocks(db);
        }
    }

    public LockedPeriodSet(UserDatabaseDTO userDatabaseDTO) {
        indexLocks(userDatabaseDTO);
    }

    public LockedPeriodSet(ActivityFormDTO activity) {
        for (LockedPeriodDTO lock : activity.getLockedPeriods()) {
            if (lock.isEnabled()) {
                if (lock.getParentType().equals(ProjectDTO.ENTITY_NAME)) {
                    projectLocks.put(lock.getParentId(), lock);
                } else {
                    activityLocks.put(activity.getId(), lock);
                }
            }
        }
    }

    public LockedPeriodSet(ActivityDTO activity) {
        indexLocks(activity.getDatabase());
    }


    private void indexLocks(UserDatabaseDTO db) {
        for (ActivityDTO activity : db.getActivities()) {
            addEnabled(activityLocks, activity.getId(), db.getLockedPeriods());
            addEnabled(activityLocks, activity.getId(), activity.getLockedPeriods());
        }
        for (ProjectDTO project : db.getProjects()) {
            addEnabled(projectLocks, project.getId(), project.getLockedPeriods());
        }
    }

    private void addEnabled(Multimap<Integer, LockedPeriodDTO> map, int key, Iterable<LockedPeriodDTO> periods) {
        for (LockedPeriodDTO lock : periods) {
            if (lock.isEnabled()) {
                map.put(key, lock);
            }
        }
    }

    public boolean isLocked(FormInstance instance, FormClass formClass) {
        Date endDate = BuiltinFields.getDateRange(instance, formClass).getEnd();
        if (endDate != null) {
            int activityId = CuidAdapter.getLegacyIdFromCuid(formClass.getId());
            int projectId = -1;
            FieldValue projectValue = BuiltinFields.getProjectValue(instance, formClass);
            if (projectValue instanceof ReferenceValue && !((ReferenceValue) projectValue).getReferences().isEmpty() ) {
                projectId = CuidAdapter.getLegacyIdFromCuid(((ReferenceValue) projectValue).getReferences().iterator().next().getRecordId());
            }
            return isLocked(activityId, new LocalDate(endDate), projectId);
        }
        return false;
    }

    public boolean isLocked(SiteDTO site) {
        int projectId = site.getProject() != null ? site.getProject().getId() : -1;
        return isLocked(site.getActivityId(), site.getDate2(), projectId);
    }

    public boolean isLocked(int activityId, LocalDate endDate, int projectId) {
        if (endDate == null) { // for monthly sites end date is null
            return false;
        }

        if (isActivityLocked(activityId, endDate)) {
            return true;
        }
        if (projectId != -1) {
            if (dateRangeLocked(endDate, projectLocks.get(projectId))) {
                return true;
            }
        }
        return false;
    }

    public boolean isActivityLocked(int activityId, LocalDate date) {
        return dateRangeLocked(date, activityLocks.get(activityId));
    }

    public boolean isActivityLocked(int activityId, Date date) {
        return isActivityLocked(activityId, new LocalDate(date));
    }

    public boolean isProjectLocked(int projectId, LocalDate date) {
        return dateRangeLocked(date, projectLocks.get(projectId));
    }

    public boolean isProjectLocked(int projectId, Date date) {
        return isProjectLocked(projectId, new LocalDate(date));
    }

    private boolean dateRangeLocked(LocalDate date, Collection<LockedPeriodDTO> locks) {
        if (date == null) { // for monthly sites end date is null
            return false;
        }

        for (LockedPeriodDTO lock : locks) {
            if (lock.fallsWithinPeriod(date)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLocks() {
        return !activityLocks.isEmpty() || !projectLocks.isEmpty();
    }
}
