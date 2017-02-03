package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;


public class CodeMirrorWidget extends Composite implements HasValue<String> {

    private static boolean resourcesInjected = false;

    private CodeMirrorEditor editor;

    public CodeMirrorWidget() {
        initWidget(new SimplePanel());

        if(!resourcesInjected) {

            ScriptInjector.fromString(CodeMirrorBundle.INSTANCE.script().getText())
                    .setRemoveTag(false)
                    .setWindow(ScriptInjector.TOP_WINDOW)
                    .inject();

            StyleInjector.inject(CodeMirrorBundle.INSTANCE.styles().getText());
            resourcesInjected = true;
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        this.editor = setup(this, getElement());
    }



    private native CodeMirrorEditor setup(CodeMirrorWidget widget, JavaScriptObject element) /*-{
        var instance = $wnd.CodeMirror(
            element,
            {
                mode: 'activityinfo',
                theme: 'default',
                viewportMargin: Infinity
            }
        );
        // Listener for changes and propagate them back into the GWT compiled code
        instance.on("change", function () {
            $entry(widget.@org.activityinfo.ui.codemirror.client.CodeMirrorWidget::handleChange());
        });
        return instance;
    }-*/;

    private void handleChange() {
        ValueChangeEvent.fire(this, editor.getDoc().getValue());
    }

    @Override
    public String getValue() {
        return editor.getDoc().getValue();
    }

    @Override
    public void setValue(String value) {
        setValue(value, true);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        editor.getDoc().setValue(value);
        if(fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
        return addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}
