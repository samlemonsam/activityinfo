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

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.promise.Promise;


public final class IDBFactoryImpl extends JavaScriptObject implements IDBFactory {

    protected IDBFactoryImpl() {
    }

    public static native IDBFactoryImpl create() /*-{
        return $wnd.indexedDB || $wnd.mozIndexedDB || $wnd.webkitIndexedDB || $wnd.msIndexedDB;
    }-*/;

    @Override
    public native void open(String databaseName, int version, IDBOpenDatabaseCallback callback) /*-{
        var request = this.open(databaseName, version);
        request.onerror = function(event) {
            callback.@IDBOpenDatabaseCallback::onError(*)(event);
        };
        request.onupgradeneeded = function(event) {
            var db = event.target.result;
            var oldVersion = event.oldVersion;
            callback.@IDBOpenDatabaseCallback::onUpgradeNeeded(*)(db, oldVersion);

        };
        request.onsuccess = function(event) {
            var db = @IDBFactoryImpl::wrap(*)(request.result);
            callback.@IDBOpenDatabaseCallback::onSuccess(*)(db);
        };
    }-*/;

    @Override
    public native Promise<Void> deleteDatabase(String name) /*-{
        var promise = @Promise::new()();
        var request = this.deleteDatabase(name);
        request.onsuccess = function(event) {
            promise.@Promise::onSuccess(*)(null);
        }
        request.onerror = function(event) {
            promise.@Promise::onFailure(*)(@RuntimeException::new()());
        }
        return promise;
    }-*/;

    private static IDBDatabaseImpl wrap(JavaScriptObject db) {
        return db.cast();
    }
}
