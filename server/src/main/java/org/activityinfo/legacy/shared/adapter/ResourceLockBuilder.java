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
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.lock.ResourceLock;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

/**
 * @author yuriyz on 10/07/2015.
 */
public class ResourceLockBuilder {

    private LockedPeriodDTO lockedPeriodDTO;

    public ResourceLockBuilder(LockedPeriodDTO lockedPeriodDTO) {
        Preconditions.checkNotNull(lockedPeriodDTO);
        this.lockedPeriodDTO = lockedPeriodDTO;
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
        return null;
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

    public static List<ResourceLock> fromLockedPeriods(Collection<LockedPeriodDTO> periods) {
        List<ResourceLock> locks = Lists.newArrayList();
        for (LockedPeriodDTO period : periods) {
            locks.add(new ResourceLockBuilder(period).build());
        }
        return locks;
    }
}
