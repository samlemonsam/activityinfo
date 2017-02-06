package org.activityinfo.ui.codemirror.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

import java.util.logging.Logger;


public class CodeMirrorWidget extends Composite implements HasValue<String> {

    private static final Logger LOGGER = Logger.getLogger(CodeMirrorWidget.class.getName());

    private static boolean resourcesInjected = false;

    private CodeMirrorEditor editor;
    private Linter linter;

    public CodeMirrorWidget(Linter linter) {
        this.linter = linter;
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
        this.editor = setup(this, linter, getElement());
    }

    private native CodeMirrorEditor setup(CodeMirrorWidget widget, Linter linter, JavaScriptObject element) /*-{
        var editor = $wnd.CodeMirror(
            element,
            {
                mode: 'activityinfo',
                theme: 'default',
                viewportMargin: Infinity,
                lint: function(text, options) {
                    return [];
                },
            }
        );
        editor.on("change", function () {
            $entry(widget.@org.activityinfo.ui.codemirror.client.CodeMirrorWidget::handleChange()());
        });
        return editor;
    }-*/;

    private void handleChange() {
        LOGGER.info("CodeMirror change event fired.");
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
