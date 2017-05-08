package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;


public class IDBTransactionImpl extends JavaScriptObject implements IDBTransaction {


    protected IDBTransactionImpl() {
    }

    public native final IDBObjectStoreImpl objectStore(String name) /*-{
        return this.objectStore(name);
    }-*/;


}

