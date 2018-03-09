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
package org.activityinfo.ui.client.store.offline;

/**
 * Describes the current state of the offline system
 */
public class OfflineStatus {

    private SnapshotStatus snapshot;
    private int pendingChangeCount;
    private int offlineFormCount;


    public OfflineStatus(SnapshotStatus snapshot, PendingStatus pendingStatus) {
        this.snapshot = snapshot;
        pendingChangeCount = pendingStatus.getCount();
    }

    public int getOfflineFormCount() {
        return offlineFormCount;
    }

    public int getPendingChangeCount() {
        return pendingChangeCount;
    }

    public boolean isSynced() {
        return pendingChangeCount == 0 && !snapshot.isEmpty();
    }
}
