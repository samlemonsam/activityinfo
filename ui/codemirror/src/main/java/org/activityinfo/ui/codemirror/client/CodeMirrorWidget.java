package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;

import java.util.logging.Logger;


public class CodeMirrorWidget extends Composite implements RequiresResize {

    private static final Logger LOGGER = Logger.getLogger(CodeMirrorWidget.class.getName());

    private static boolean resourcesInjected = false;

    private CodeMirrorEditor editor;

    public CodeMirrorWidget() {
        initWidget(new SimpleLayoutPanel());

        if(!resourcesInjected) {

            ScriptInjector.fromString(CodeMirrorBundle.INSTANCE.script().getText())
                    .setRemoveTag(false)
                    .setWindow(ScriptInjector.TOP_WINDOW)
                    .inject();

            StyleInjector.inject(CodeMirrorBundle.INSTANCE.styles().getText());
            resourcesInjected = true;
        }
        this.editor = setup(this, getElement());

    }

    @Override
    protected void onAttach() {
        super.onAttach();
    }


    private native CodeMirrorEditor setup(CodeMirrorWidget widget, JavaScriptObject element) /*-{
        var editor = $wnd.CodeMirror(
            element,
            {
                mode: 'activityinfo',
                theme: 'default',
                viewportMargin: Infinity,
                matchBrackets: true
            }
        );
        return editor;
    }-*/;

    private native void addEventHandler(CodeMirrorEditor editor, String eventName, CodeMirrorEventHandler handler) /*-{
        editor.on(eventName, function () {
            $entry(handler.@org.activityinfo.ui.codemirror.client.CodeMirrorEventHandler::onEvent()());
        });
    }-*/;

    public CodeMirrorEditor getEditor() {
        return editor;
    }

    public void addChangeHandler(CodeMirrorEventHandler handler) {
        addEventHandler(editor, "change", handler);
    }

    @Override
    public void onResize() {
        ((SimpleLayoutPanel)getWidget()).onResize();
        getEditor().refresh();
    }
}
