package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.JavaScriptObject;
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

    private static void fail(AsyncCallback<?> callback, JavaScriptObject error) {
        callback.onFailure(new RuntimeException("JS failure"));
    }

    public static Promise<IndexedDB> open() {
        Promise<IndexedDB> result = new Promise<>();
        open(result);
        return result;
    }


    public final native void putSchema(FormClass formSchema, AsyncCallback<Void> callback) /*-{

        // Convert to "clonable" javascript object
        var schema = JSON.parse(formSchema.@FormClass::toJsonString()());

        // Start a new transaction
        var tx = this.db.transaction("formSchemas", "readwrite");
        tx.onerror = function(event) {
            console.log("putSchema Error: " + event);
        }
        tx.oncomplete = function(event) {
            callback.@AsyncCallback::onSuccess(*)(null);
            console.log("putSchema complete.");
        }
        var schemaStore = tx.objectStore("formSchemas");
        schemaStore.put(schema);
    }-*/;

    public final Promise<Void> putSchema(FormClass schema) {
        Promise<Void> result = new Promise<>();
        putSchema(schema, result);
        return result;
    }

    public final Promise<FormClass> loadSchema(ResourceId formId) {
        Promise<FormClass> result = new Promise<>();
        loadSchema(formId.asString(), result);
        return result;
    }

    public final void loadSchema(ResourceId formId, AsyncCallback<FormClass> callback) {
        loadSchema(formId.asString(), callback);
    }

    private native void loadSchema(String formId, AsyncCallback<FormClass> callback) /*-{

        var tx = this.db.transaction("formSchemas", "readonly");
        tx.onerror = function(event) {
            console.log("loadSchema Error: " + event);
            @IndexedDB::fail(*)(callback, event);
        }
        tx.oncomplete = function(event) {
            console.log("loadSchema tx completed");
        }
        var schemaStore = tx.objectStore("formSchemas");
        var schemaReq = schemaStore.get(formId);
        schemaReq.onsuccess = function(event) {
            var schemaJson = JSON.stringify(event.target.result);
            var schema = @FormClass::fromJson(Ljava/lang/String;)(schemaJson);
            callback.@AsyncCallback::onSuccess(*)(schema);
        }
    }-*/;
}
