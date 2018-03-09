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

import org.activityinfo.model.resource.ResourceId;

import java.util.Set;

/**
 * Provides information on the status of pending entries
 */
public class PendingStatus {

    /**
     * The ids of forms with pending updates in the pending queue.
     */
    private Set<ResourceId> forms;
    private int count;

    public PendingStatus(int count, Set<ResourceId> forms) {
        this.count = count;
        this.forms = forms;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public Set<ResourceId> getForms() {
        return forms;
    }

    /**
     * @return the total number of pending transactions.
     */
    public int getCount() {
        return count;
    }
}
