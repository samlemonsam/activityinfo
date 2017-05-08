package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;


public class IDBTransaction extends JavaScriptObject {


    protected IDBTransaction() {
    }

    private native ObjectStore objectStore(String name) /*-{
        return this.objectStore(name);
    }-*/;

    public final SchemaStore schemas() {
        return objectStore(SchemaStore.NAME).cast();
    }

    public final RecordStore records() {
        return objectStore(RecordStore.NAME).cast();
    }
}

