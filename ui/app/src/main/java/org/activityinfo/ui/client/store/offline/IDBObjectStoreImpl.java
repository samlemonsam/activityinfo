package org.activityinfo.ui.client.store.offline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.json.JsonValue;
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
    public final native void put(JsonValue object) /*-{
        this.put(object);
    }-*/;

    @Override
    public final native void put(String key, JsonValue object) /*-{
        this.put(object, key);
    }-*/;

    protected final native void get(JavaScriptObject key, AsyncCallback<JsonValue> callback) /*-{
        var request = this.get(key);
        request.onsuccess = function(event) {
            var object = event.target.result;
            callback.@AsyncCallback::onSuccess(*)(object);
        }
        request.onerror = function(event) {
            callback.@AsyncCallback::onFailure(*)(@RuntimeException::new()());
        }
    }-*/;


    @Override
    public final Promise<JsonValue> get(String key) {
        return get(new String[] { key });
    }

    @Override
    public final Promise<JsonValue> get(String[] keys) {
        Promise<JsonValue> result = new Promise<>();
        get(createKey(keys), result);
        return result;
    }

    /**
     *  @param lowerBound the lower bound of the key range (inclusive)
     * @param upperBound the upper bound of the key range (inclusive)
     * @param callback
     */
    @Override
    public final native void openCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback callback) /*-{
        var request = this.openCursor($wnd.IDBKeyRange.bound(lowerBound, upperBound));
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
