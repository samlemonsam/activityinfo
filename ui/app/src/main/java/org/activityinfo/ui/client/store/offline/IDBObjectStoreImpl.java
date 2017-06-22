package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
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

    @Override
    public final native void putJson(String json, String key) /*-{
        this.put(JSON.parse(json), key);
    }-*/;

    protected final native void getJson(JavaScriptObject key, AsyncCallback<String> callback) /*-{
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
        return getJson(new String[] { key });
    }

    @Override
    public final Promise<String> getJson(String[] keys) {
        Promise<String> result = new Promise<>();
        getJson(createKey(keys), result);
        return result;
    }

    /**
     *  @param lowerBound the lower bound of the key range (inclusive)
     * @param upperBound the upper bound of the key range (inclusive)
     * @param callback
     */
    @Override
    public final native void openKeyCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback callback) /*-{
        var request = this.openKeyCursor($wnd.IDBKeyRange.bound(lowerBound, upperBound));
        request.onsuccess = function(event) {
          var cursor = event.target.result;
            if(cursor) {
                callback.@IDBCursorCallback::onNext(*)(cursor);
            } else {
                callback.@IDBCursorCallback::onDone()();
            }
        };
    }-*/;

    private JavaScriptObject createKey(String[] keys) {
        if(keys.length == 1) {
            return createKey(keys[0]);
        }
        JsArrayString array = JsArrayString.createArray().cast();
        for (String key : keys) {
            array.push(key);
        }
        return array;
    }

    private static native JavaScriptObject createKey(String key) /*-{
        return key;
    }-*/;

}
