package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.ui.client.widget.TextBox;


public class TextTypeEditor extends TypeEditor<TextType> {

    interface TextTypeEditorUiBinder extends UiBinder<FlowPanel, TextTypeEditor> {
    }

    private static TextTypeEditorUiBinder ourUiBinder = GWT.create(TextTypeEditorUiBinder.class);

    private final FlowPanel panel;

    @UiField
    TextBox inputMask;

    public TextTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof TextType;
    }

    @Override
    protected void show(TextType type) {
        inputMask.setText(type.getInputMask());
    }

    @UiHandler("inputMask")
    public void onInputMaskChanged(KeyUpEvent event) {
        updateType(TextType.SIMPLE.withInputMask(inputMask.getText()));
    }
}
