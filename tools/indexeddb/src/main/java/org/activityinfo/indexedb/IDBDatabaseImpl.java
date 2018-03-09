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
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

/**
 * IndexedDB access
 */
public final class IDBDatabaseImpl extends JavaScriptObject implements IDBDatabase {

    protected IDBDatabaseImpl() {}


    @Override
    public native void transaction(String[] objectStores, String mode, IDBTransactionCallback callback) /*-{
        var tx = this.transaction(objectStores, mode);
        tx.onerror = function(event) {
            console.log("transact error: " + event);
            callback.@IDBTransactionCallback::onError(*)(event);
        }
        tx.onabort = function(event) {
            console.log("transact error: " + event);
            callback.@IDBTransactionCallback::onAbort(*)(event);
        }
        tx.oncomplete = function(event) {
            console.log("transact completed");
            callback.@IDBTransactionCallback::onComplete(*)(event);
        }

        callback.@IDBTransactionCallback::execute(*)(@IDBDatabaseImpl::wrap(*)(tx));
    }-*/;

    private static IDBTransactionImpl wrap(JavaScriptObject jso) {
        return jso.cast();
    }

    @Override
    public final native void close() /*-{
        this.close();
    }-*/;

}
