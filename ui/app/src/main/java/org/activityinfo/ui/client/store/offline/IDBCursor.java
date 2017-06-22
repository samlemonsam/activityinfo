package org.activityinfo.ui.client.store.offline;


import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static elemental2.core.Global.JSON;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface IDBCursor {

    /**
     * Advances the cursor to the next position along its direction, to the
     * item whose key matches the optional key parameter.
     */
    @JsMethod(name = "continue")
    void continue_();

    @JsProperty(name = "key")
    String getKeyString();

    @JsProperty(name = "key")
    String[] getKeyArray();

    @JsProperty(name = "value")
    Object getValue();

    default String getValueAsJson() {
        return JSON.stringify(getValue());
    }

}
