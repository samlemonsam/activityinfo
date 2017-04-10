package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.widget.TextBox;

/**
 * Allows the user to edit the properties of a quantity type
 */
public class QuantityTypeEditor extends TypeEditor<QuantityType> {

    interface QuantityTypePanelUiBinder extends UiBinder<FlowPanel, QuantityTypeEditor> {
    }

    private static QuantityTypePanelUiBinder ourUiBinder = GWT.create(QuantityTypePanelUiBinder.class);


    private final FlowPanel panel;

    @UiField
    TextBox units;

    public QuantityTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof QuantityType;
    }

    @Override
    protected void show(QuantityType type) {
        units.setText(type.getUnits());
    }

    @UiHandler("units")
    public void onUnitsChange(KeyPressEvent event) {
        updateType(currentType().withUnits(units.getValue()));
    }
}