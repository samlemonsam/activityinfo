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

        callback.@IDBTransactionCallback::execute(*)(tx);
    }-*/;

    @Override
    public final native void close() /*-{
        this.close();
    }-*/;

}
