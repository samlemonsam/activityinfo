package org.activityinfo.ui.codemirror.client;


import com.google.gwt.core.client.JavaScriptObject;

public class LeftTop extends JavaScriptObject {

    protected LeftTop() {
    }

    public static native LeftTop create(int left, int top) /*-{
        return { left: left, top: top };
    }-*/;

    public final native int getLeft() /*-{
        return this.left;
    }-*/;

    public final native int getTop() /*-{
        return this.top;
    }-*/;

}
