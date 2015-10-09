package org.activityinfo.legacy.shared.adapter;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.appengine.repackaged.com.google.api.client.util.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.lock.ResourceLock;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author yuriyz on 10/07/2015.
 */
public class ActivityFormLockBuilder {

    private LockedPeriodDTO lockedPeriodDTO;
    private ResourceId classId;

    public ActivityFormLockBuilder(LockedPeriodDTO lockedPeriodDTO, int activityId) {
        this(lockedPeriodDTO, CuidAdapter.activityFormClass(activityId));
    }

    public ActivityFormLockBuilder(@Nonnull LockedPeriodDTO lockedPeriodDTO, @Nonnull ResourceId classId) {
        Preconditions.checkNotNull(lockedPeriodDTO);
        Preconditions.checkNotNull(classId);

        this.lockedPeriodDTO = lockedPeriodDTO;
        this.classId = classId;
    }

    public ResourceLock build() {
        ResourceLock lock = new ResourceLock();
        lock.setId(CuidAdapter.lockId(lockedPeriodDTO.getId()));
        lock.setEnabled(lockedPeriodDTO.isEnabled());
        lock.setName(lockedPeriodDTO.getName());
        lock.setOwnerId(createOwnerId());
        lock.setExpression(createExpression());
        return lock;
    }

    private String createExpression() {
        String endDateId = ActivityFormClassBuilder.createEndDateField(classId).getId().asString();
        long start = lockedPeriodDTO.getFromDate().atMidnightInMyTimezone().getTime();
        long end = lockedPeriodDTO.getToDate().atMidnightInMyTimezone().getTime();

        String expression = "(" + start + "<={" + endDateId + "})" +
                "&&" +
                "({" + endDateId + "}<=" + end + ")";
        if (!Strings.isNullOrEmpty(lockedPeriodDTO.getParentType())) {
            if (ActivityDTO.ENTITY_NAME.equals(lockedPeriodDTO.getParentType())) {
                if (lockedPeriodDTO.getParentId() != CuidAdapter.getLegacyIdFromCuid(classId)) {
                    throw new RuntimeException("Lock activity id does not match activity id provided to builder! " +
                            "It means that form class is not owner of the lock.");
                }
            } else if (ProjectDTO.ENTITY_NAME.equals(lockedPeriodDTO.getParentType())) {
                String projectId = CuidAdapter.field(classId, CuidAdapter.PROJECT_FIELD).asString();
                String lockProjectValue = CuidAdapter.projectInstanceId(lockedPeriodDTO.getParentId()).asString();
                expression = expression + "&&"
                        + "({" + projectId + "}==" + lockProjectValue + ")";
            }

        }
        return expression;
    }

    private ResourceId createOwnerId() {
        if (UserDatabaseDTO.ENTITY_NAME.equals(lockedPeriodDTO.getParentType())) {
            return CuidAdapter.databaseId(lockedPeriodDTO.getParentId());
        } else if (ActivityDTO.ENTITY_NAME.equals(lockedPeriodDTO.getParentType())) {
            return CuidAdapter.activityFormClass(lockedPeriodDTO.getParentId());
        } else if (ProjectDTO.ENTITY_NAME.equals(lockedPeriodDTO.getParentType())) {
            return CuidAdapter.projectFormClass(lockedPeriodDTO.getParentId());
        }
        return null;
    }

    public static ResourceLock fromLock(LockedPeriodDTO period, ResourceId activityId) {
        return new ActivityFormLockBuilder(period, activityId).build();
    }

    public static List<ResourceLock> fromLockedPeriods(Collection<LockedPeriodDTO> periods, ResourceId activityId) {
        List<ResourceLock> locks = Lists.newArrayList();
        for (LockedPeriodDTO period : periods) {
            locks.add(new ActivityFormLockBuilder(period, activityId).build());
        }
        return locks;
    }
}
