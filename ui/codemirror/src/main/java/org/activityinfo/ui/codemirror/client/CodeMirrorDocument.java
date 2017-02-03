package org.activityinfo.ui.codemirror.client;

import jsinterop.annotations.JsType;

/**
 * Interface to the CodeMirrorDocument
 */
@JsType(isNative = true)
public interface CodeMirrorDocument {

    String getValue();

    void setValue(String value);

    boolean somethingSelected();

}
