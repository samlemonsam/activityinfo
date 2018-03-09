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
package org.activityinfo.indexedb;

import org.activityinfo.promise.Promise;

import java.util.HashMap;
import java.util.Map;


public class IDBFactoryStub implements IDBFactory {


    private final Map<String, IDBDatabaseStub> databaseMap = new HashMap<>();


    @Override
    public void open(String databaseName, int version, IDBOpenDatabaseCallback callback) {
        IDBDatabaseStub db = databaseMap.get(databaseName);
        if(db == null) {
            db = new IDBDatabaseStub(databaseName);
            databaseMap.put(databaseName, db);
        }

        db.maybeUpgrade(version, callback);

        callback.onSuccess(db);
    }

    @Override
    public Promise<Void> deleteDatabase(String name) {
        throw new UnsupportedOperationException();
    }

}
