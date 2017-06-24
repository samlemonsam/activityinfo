package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class ObjectStoreOptions {

    private Object keyPath;
    private boolean autoIncrement;

    private ObjectStoreOptions() {
    }

    /**
     * The key path to be used by the new object store.
     * If empty or not specified, the object store is created without a key path and uses out-of-line keys.
     * You can also pass in an array as a keyPath.
     *
     * @param key the name of the property to use as a key
     */
    @JsOverlay
    public ObjectStoreOptions setKey(String key) {
        this.keyPath = key;
        return this;
    }

    @JsOverlay
    public ObjectStoreOptions setKeyPath(String... keys) {
        this.keyPath = keys;
        return this;
    }

    @JsOverlay
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
}
