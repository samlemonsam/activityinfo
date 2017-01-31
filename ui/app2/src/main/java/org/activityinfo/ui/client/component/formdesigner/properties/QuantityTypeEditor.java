package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.widget.TextBox;

/**
 * Allows the user to edit the properties of a quantity type
 */
public class QuantityTypeEditor implements IsWidget {



    interface QuantityTypePanelUiBinder extends UiBinder<FlowPanel, QuantityTypeEditor> {
    }

    private static QuantityTypePanelUiBinder ourUiBinder = GWT.create(QuantityTypePanelUiBinder.class);


    private final FlowPanel panel;

    @UiField
    TextBox units;

    private FieldWidgetContainer currentField;

    public QuantityTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void show(FieldWidgetContainer container) {
        if(container.getFormField().getType() instanceof QuantityType) {
            this.currentField = container;
            QuantityType type = (QuantityType) container.getFormField().getType();
            units.setText(type.getUnits());
            panel.setVisible(true);

        } else {
            panel.setVisible(false);
        }
    }

    @UiHandler("units")
    public void onUnitsChange(KeyPressEvent event) {
        ((QuantityType) currentField.getFormField().getType()).setUnits(units.getValue());
        currentField.syncWithModel();
    }
}