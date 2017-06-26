package org.activityinfo.indexedb;

import com.google.gwt.core.client.JavaScriptObject;


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

    private static IDBDatabaseImpl wrap(JavaScriptObject db) {
        return db.cast();
    }
}
