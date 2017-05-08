package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.promise.Promise;

/**
 * Base class for typed IndexedDb object stores.
 *
 * <p>A new subclass should be defined for each new IndexedDb with typed
 * get and put methods.</p>
 */
public class IDBObjectStoreImpl extends JavaScriptObject implements IDBObjectStore {
    protected IDBObjectStoreImpl() {
    }

    @Override
    public final native void putJson(String json) /*-{
        this.put(JSON.parse(json));
    }-*/;

    protected final native void getJson(String key, AsyncCallback<String> callback) /*-{
        var request = this.get(key);
        request.onsuccess = function(event) {
            var object = event.target.result;
            callback.@AsyncCallback::onSuccess(*)(JSON.stringify(object));
        }
        request.onerror = function(event) {
            callback.@AsyncCallback::onFailure(*)(@RuntimeException::new()());
        }
    }-*/;

    @Override
    public final Promise<String> getJson(String key) {
        Promise<String> result = new Promise<>();
        getJson(key, result);
        return result;
    }
}
