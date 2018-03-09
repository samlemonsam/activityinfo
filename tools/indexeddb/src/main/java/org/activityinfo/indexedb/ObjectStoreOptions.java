/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.indexedb;

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
    public Object getKeyPath() {
        return keyPath;
    }

    @JsOverlay
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @JsOverlay
    public static ObjectStoreOptions withKey(String keyName) {
        ObjectStoreOptions options = new ObjectStoreOptions();
        options.keyPath = keyName;
        return options;
    }

    @JsOverlay
    public static ObjectStoreOptions withKeyPath(String... keyPath) {
        ObjectStoreOptions options = new ObjectStoreOptions();
        options.keyPath = keyPath;
        return options;
    }

    @JsOverlay
    public static ObjectStoreOptions withDefaults() {
        return new ObjectStoreOptions();
    }

    @JsOverlay
    public static ObjectStoreOptions withAutoIncrement() {
        ObjectStoreOptions options = new ObjectStoreOptions();
        options.setAutoIncrement(true);
        return options;
    }

    @JsOverlay
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
}
