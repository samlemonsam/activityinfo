package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.widget.TextBox;


public class TextTypeEditor implements IsWidget {
    interface TextTypeEditorUiBinder extends UiBinder<FlowPanel, TextTypeEditor> {
    }

    private static TextTypeEditorUiBinder ourUiBinder = GWT.create(TextTypeEditorUiBinder.class);


    private final FlowPanel panel;

    @UiField
    TextBox inputMask;

    private FieldWidgetContainer currentField;

    public TextTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void show(FieldWidgetContainer container) {
        if(container.getFormField().getType() instanceof TextType) {
            this.currentField = container;
            TextType type = (TextType)container.getFormField().getType();
            inputMask.setText(type.getInputMask());
            panel.setVisible(true);

        } else {
            panel.setVisible(false);
        }
    }

    @UiHandler("inputMask")
    public void onInputMaskChanged(KeyUpEvent event) {
        currentField.getFormField().setType(TextType.SIMPLE.withInputMask(inputMask.getText()));
        currentField.syncWithModel();
    }
}
