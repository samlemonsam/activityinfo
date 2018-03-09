/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
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
    public void onUnitsChange(KeyUpEvent event) {
        updateType(currentType().withUnits(units.getValue()));
    }
}