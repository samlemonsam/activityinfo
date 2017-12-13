package org.activityinfo.ui.codemirror.client;


public class CodeMirror {

    public native static Pos create(int line, int column) /*-{
        return $wnd.CodeMirror.Pos(line, column);
    }-*/;
}
