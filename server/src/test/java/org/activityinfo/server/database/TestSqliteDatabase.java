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
package org.activityinfo.server.database;

import com.bedatadriven.rebar.sql.client.fn.TxAsyncFunction;
import com.bedatadriven.rebar.sql.server.jdbc.SqliteStubDatabase;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by yuriy on 12/23/2014.
 */
public class TestSqliteDatabase extends SqliteStubDatabase {

    public TestSqliteDatabase(String databaseName) {
        super(databaseName);
    }

    @Override
    public void executeUpdates(String json, AsyncCallback<Integer> callback) {
        super.executeUpdates(adjustExecuteUpdates(json), callback);
    }

    public String adjustExecuteUpdates(String json) {
        return json;
    }

    @Override
    public <T> void execute(TxAsyncFunction<Void, T> f, AsyncCallback<T> callback) {
        super.execute(f, callback);
    }
}
