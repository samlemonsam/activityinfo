package org.activityinfo.ui.codemirror.client;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface Token {

    /**
     * The character (on the given line) at which the token starts.
     */
    @JsProperty
    int getStart();

    /**
     * The character at which the token ends.
     */
    @JsProperty
    int getEnd();

    /**
     * The token's string.
     */
    @JsProperty
    String getString();


    /**
     * The token type the mode assigned to the token, such as "keyword" or "comment" (may also be null).
     */
    @JsProperty
    String getType();


}
