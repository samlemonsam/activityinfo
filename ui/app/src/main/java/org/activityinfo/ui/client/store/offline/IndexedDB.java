package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * IndexedDB access
 */
public class IndexedDB extends JavaScriptObject {

    protected IndexedDB() {}

    public static native void open(AsyncCallback<IndexedDB> callback) /*-{

        var api = {};
        api.indexedDb = $wnd.indexedDB || $wnd.mozIndexedDB || $wnd.webkitIndexedDB || $wnd.msIndexedDB;
        api.IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.msIDBTransaction || {READ_WRITE: "readwrite"}; // This line should only be needed if it is needed to support the object's constants for older browsers
        api.IDBKeyRange = window.IDBKeyRange || window.webkitIDBKeyRange || window.msIDBKeyRange;

        var request = indexedDB.open("ActivityInfo", 2);
        request.onerror = function(event) {
            @IndexedDB::fail(*)(callback, event);
        };
        request.onupgradeneeded = function(event) {
            var db = event.target.result;
            var schemaStore = db.createObjectStore("formSchemas", { keyPath: "id" });

            var formStore = db.createObjectStore("forms", {keyPath: "formId"});

            var recordStore = db.createObjectStore("records", {keyPath: [ "formId", "recordId"]});
            recordStore.createIndex("formId", "formId", { unique: false });
            recordStore.createIndex("parentFormId", "parentFormId", { unique: false });
        };
        request.onsuccess = function(event) {
            api.db = event.target.result;
            callback.@AsyncCallback::onSuccess(*)(api);
        };
    }-*/;

    public static void fail(AsyncCallback<?> callback, JavaScriptObject error) {
        callback.onFailure(new RuntimeException("JS failure"));
    }

    public static Promise<IndexedDB> open() {
        Promise<IndexedDB> result = new Promise<>();
        open(result);
        return result;
    }

    public static TxBuilder begin(String... objectStores) {
        return new TxBuilder().objectStores(objectStores);
    }

    public static final Promise<FormClass> loadSchema(ResourceId formId) {
        return IndexedDB.begin(SchemaStore.NAME).query(tx -> tx.schemas().get(formId));
    }

    public final native void close() /*-{
        this.db.close();
    }-*/;

    private native Promise transact(JsArrayString objectStores, String mode, Work work, AsyncCallback<Void> callback) /*-{
        var tx = this.db.transaction(objectStores, mode);
        tx.onerror = function(event) {
            console.log("transact error: " + event);
            @IndexedDB::fail(*)(callback, event);
        }
        tx.oncomplete = function(event) {
            console.log("transact completed");
            callback.@AsyncCallback::onSuccess(*)(null);
        }
        return work.@org.activityinfo.ui.client.store.offline.Work::query(*)(tx);
    }-*/;


    public static class TxBuilder {
        private JsArrayString objectStores = JsArrayString.createArray().cast();
        private String mode = "readonly";

        public TxBuilder objectStores(String... names) {
            for (String name : names) {
                objectStores.push(name);
            }
            return this;
        }

        public TxBuilder objectStore(String name) {
            objectStores.push(name);
            return this;
        }

        public TxBuilder readwrite() {
            this.mode = "readwrite";
            return this;
        }

        public Promise<Void> execute(VoidWork work) {
            return query(transaction -> {
                work.execute(transaction);
                return Promise.done();
            });
        }

        public <T> Promise<T> query(Work<T> work) {
            Promise<Void> txResult = new Promise<>();
            Promise<T> queryResult = new Promise<>();
            IndexedDB.open(new AsyncCallback<IndexedDB>() {
                @Override
                public void onFailure(Throwable caught) {
                    txResult.reject(caught);
                }

                @Override
                public void onSuccess(IndexedDB db) {
                    db.transact(objectStores, mode, work, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            db.close();
                            txResult.onFailure(caught);
                        }

                        @Override
                        public void onSuccess(Void voidResult) {
                            db.close();
                            txResult.onSuccess(null);
                        }
                    }).then(queryResult);
                }
            });
            return txResult.join(finishedTx -> queryResult);
        }

    }
}
