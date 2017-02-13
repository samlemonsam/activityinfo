package org.activityinfo.ui.codemirror.client;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface TextMarker {

    /**
     * to remove the mark
     */
    void clear();

    /**
     * @return a {from, to} object (both holding document positions), indicating the current position of the
     * marked range, or undefined if the marker is no longer in the document
     */
    Range find();

    /**
     *  call if you've done something that might change the size of the marker (for example changing the content of
     *  a replacedWith node), and want to cheaply update the display
     */
    void changed();
}