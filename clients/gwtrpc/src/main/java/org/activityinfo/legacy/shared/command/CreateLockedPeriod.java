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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.LockedPeriodDTO;

public class CreateLockedPeriod implements MutatingCommand<CreateResult> {
    private int activityId = 0;
    private int databaseId = 0;
    private int projectId = 0;
    private int folderId = 0;
    private LockedPeriodDTO lockedPeriod;

    public CreateLockedPeriod() {
    }

    public CreateLockedPeriod(LockedPeriodDTO lockedPeriod) {
        this.setLockedPeriod(lockedPeriod);
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int userDatabseId) {
        this.databaseId = userDatabseId;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setLockedPeriod(LockedPeriodDTO lockedPeriod) {
        this.lockedPeriod = lockedPeriod;
    }

    public LockedPeriodDTO getLockedPeriod() {
        return lockedPeriod;
    }
}