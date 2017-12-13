package org.activityinfo.indexedb;

import com.google.gwt.core.client.JavaScriptObject;


public final class IDBTransactionImpl extends JavaScriptObject implements IDBTransaction {


    protected IDBTransactionImpl() {
    }

    public native final IDBObjectStoreImpl objectStore(String name) /*-{
        return this.objectStore(name);
    }-*/;


}

