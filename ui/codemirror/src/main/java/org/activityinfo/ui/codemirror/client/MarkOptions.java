package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;

public class MarkOptions extends JavaScriptObject {


    protected MarkOptions() {
    }

    public static native MarkOptions create() /*-{
        return {};
    }-*/;

    /**
     * Assigns a CSS class to the marked stretch of text.
     */
    public final native void setClassName(String className) /*-{
        this.className = className;
    }-*/;

    /**
     * When given, will give the nodes created for this span a HTML title attribute with the given value.
     */
    public final native void setTitle(String title) /*-{
        this.title = title;
    }-*/;


}
