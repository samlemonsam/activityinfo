package org.activityinfo.indexedb;

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
public final class IDBObjectStoreImpl<T> extends JavaScriptObject implements IDBObjectStore<T>, IDBObjectStoreUpgrade {
    protected IDBObjectStoreImpl() {
    }

    @Override
    public final native void put(T object) /*-{
        this.put(object);
    }-*/;

    @Override
    public final native void put(int key, T object) /*-{
       this.put(key, object);
    }-*/;

    @Override
    public final void put(String key, T object) {
        put2(key, object);
    }

    @Override
    public final void put(String[] key, T object) {
        put2(key, object);
    }

    private final native void put2(Object key, T object) /*-{
        this.put(object, key);
    }-*/;

    protected final native void get(JavaScriptObject key, AsyncCallback<T> callback) /*-{
        var request = this.get(key);
        request.onsuccess = function(event) {
            var object = event.target.result;
            callback.@AsyncCallback::onSuccess(*)(object);
        }
        request.onerror = function(event) {
            callback.@AsyncCallback::onFailure(*)(@RuntimeException::new()());
        }
    }-*/;

    private Promise<T> get0(JavaScriptObject key) {
        Promise<T> result = new Promise<>();
        get(key, result);
        return result;
    }


    @Override
    public final Promise<T> get(String key) {
        return get0(createStringKey(key));
    }

    @Override
    public final Promise<T> get(String[] keys) {
        return get0(createStringArrayKey(keys));
    }

    @Override
    public final Promise<T> get(int key) {
        return get0(createIntKey(key));
    }

    private static native JavaScriptObject createIntKey(int key) /*-{ return key; }-*/;
    private static native JavaScriptObject createStringKey(String key) /*-{ return key; }-*/;
    private static native JavaScriptObject createStringArrayKey(String[] key) /*-{ return key; }-*/;

    /**
     *  @param lowerBound the lower bound of the key range (inclusive)
     * @param upperBound the upper bound of the key range (inclusive)
     * @param callback
     */
    @Override
    public final native void openCursor(String[] lowerBound, String[] upperBound, IDBCursorCallback<T> callback) /*-{
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

    @Override
    public final native void openCursor(IDBCursorCallback<T> callback) /*-{
        var request = this.openCursor();
        request.onsuccess = function(event) {
            var cursor = event.target.result;
            if(cursor) {
                callback.@IDBCursorCallback::onNext(*)(cursor);
            } else {
                callback.@IDBCursorCallback::onDone()();
            }
        };
    }-*/;

    @Override
    public final native void delete(String[] key) /*-{
        this['delete'](key);
    }-*/;

    @Override
    public native void delete(String[] lowerBound, String[] upperBound) /*-{
        this['delete']($wnd.IDBKeyRange.bound(lowerBound, upperBound));
    }-*/;

    @Override
    public final native void delete(int key) /*-{
        this['delete'](key);
    }-*/;

    @Override
    public final native void createIndex(String indexName, String keyPath, IndexOptions indexOptions) /*-{
        this.createIndex(indexName, keyPath, indexOptions);
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
