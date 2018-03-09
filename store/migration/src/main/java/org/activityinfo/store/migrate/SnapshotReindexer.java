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
package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;


public class SnapshotReindexer extends MapOnlyMapper<Entity, Void> {

    private transient DatastoreMutationPool pool;

    @Override
    public void beginSlice() {
        super.beginSlice();
        pool = DatastoreMutationPool.create();
    }

    @Override
    public void map(Entity entity) {

        if(entity.getProperty("version") == null) {
            entity.setIndexedProperty("version", entity.getKey().getId());
            pool.put(entity);
        }
    }

    @Override
    public void endSlice() {
        super.endSlice();
        pool.flush();
    }
}
