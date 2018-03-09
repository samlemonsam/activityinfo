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

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

public class PendingStatusQuery extends SimpleTask<PendingStatus> {
    private OfflineDatabase database;

    public PendingStatusQuery(OfflineDatabase database) {
        this.database = database;
    }

    @Override
    protected Promise<PendingStatus> execute() {
        return database
            .begin(PendingStore.DEF)
            .query(tx -> tx.objectStore(PendingStore.DEF).getStatus());
    }
}