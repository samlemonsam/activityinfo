package org.activityinfo.indexedb;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IndexOptions {

    public boolean unique;


    public static IndexOptions nonUnique() {
        IndexOptions options = new IndexOptions();
        options.unique = false;
        return options;
    }
}
