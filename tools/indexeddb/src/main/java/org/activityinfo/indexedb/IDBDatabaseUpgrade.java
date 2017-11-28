package org.activityinfo.indexedb;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface IDBDatabaseUpgrade {

    @JsMethod
    <T> IDBObjectStoreUpgrade createObjectStore(String name, ObjectStoreOptions options);

}
