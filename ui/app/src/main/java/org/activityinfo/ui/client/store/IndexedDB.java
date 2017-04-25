package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

/**
 * IndexedDB access
 */
public class IndexedDB extends JavaScriptObject {

    protected IndexedDB() {}

    public static native void open(IDBCallback<IndexedDB> callback) /*-{

        var api = {};
        api.indexedDb = $wnd.indexedDB || $wnd.mozIndexedDB || $wnd.webkitIndexedDB || $wnd.msIndexedDB;
        api.IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.msIDBTransaction || {READ_WRITE: "readwrite"}; // This line should only be needed if it is needed to support the object's constants for older browsers
        api.IDBKeyRange = window.IDBKeyRange || window.webkitIDBKeyRange || window.msIDBKeyRange;

        var request = indexedDB.open("ActivityInfo", 2);
        request.onerror = function(event) {
            callback.@IDBCallback::onFailure(*)(event);
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
            callback.@IDBCallback::onSuccess(*)(api);
        };
    }-*/;


    public final native void putSchema(FormClass formSchema) /*-{

        // Convert to "clonable" javascript object
        var schema = JSON.parse(formSchema.@FormClass::toJsonString()());

        // Start a new transaction
        var tx = this.db.transaction("formSchemas", "readwrite");
        tx.onerror = function(event) {
            console.log("putSchema Error: " + event);
        }
        tx.oncomplete = function(event) {
            console.log("putSchema complete.");
        }
        var formStore = tx.objectStore("forms");
        var schemaStore = tx.objectStore("formSchemas");
        schemaStore.put(schema);
    }-*/;

    public final void loadSchema(ResourceId formId, IDBCallback<FormClass> callback) {
        loadSchema(formId.asString(), callback);
    }

    private native void loadSchema(String formId, IDBCallback<FormClass> callback) /*-{

        var tx = this.db.transaction("formSchemas", "readonly");
        tx.onerror = function(event) {
            console.log("loadSchema Error: " + event);
            callback.@IDBCallback::onFailure(*)(event);
        }
        tx.oncomplete = function(event) {
            console.log("loadSchema tx completed");
        }
        var schemaStore = tx.objectStore("formSchemas");
        var schemaReq = schemaStore.get(formId);
        schemaReq.onsuccess = function(event) {
            var schemaJson = JSON.stringify(event.target.result);
            var schema = @FormClass::fromJson(Ljava/lang/String;)(schemaJson);
            callback.@IDBCallback::onSuccess(*)(schema);
        }
    }-*/;
}
