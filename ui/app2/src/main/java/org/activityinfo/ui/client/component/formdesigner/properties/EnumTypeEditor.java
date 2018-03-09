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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumType;

/**
 * Allows the user to edit the properties of a quantity type
 */
public class EnumTypeEditor extends TypeEditor<EnumType> {

    interface EnumTypeEditorPanelUiBinder extends UiBinder<FlowPanel, EnumTypeEditor> {
    }

    private static EnumTypeEditorPanelUiBinder ourUiBinder = GWT.create(EnumTypeEditorPanelUiBinder.class);


    private final FlowPanel panel;

    @UiField
    RadioButton automaticPresentation;
    @UiField
    RadioButton checkboxPresentation;
    @UiField
    RadioButton dropdownPresentation;


    public EnumTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof EnumType && ((EnumType) type).getCardinality() == Cardinality.SINGLE;
    }

    @Override
    protected void show(EnumType type) {
        automaticPresentation.setValue(type.getPresentation() == EnumType.Presentation.AUTOMATIC);
        checkboxPresentation.setValue(type.getPresentation() == EnumType.Presentation.RADIO_BUTTON);
        dropdownPresentation.setValue(type.getPresentation() == EnumType.Presentation.DROPDOWN);
    }

    @UiHandler("automaticPresentation")
    void onAutomaticSelected(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.AUTOMATIC);
    }

    @UiHandler("checkboxPresentation")
    void onCheckBoxPresentation(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.RADIO_BUTTON);
    }

    @UiHandler("dropdownPresentation")
    void onDropdownPresentation(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.DROPDOWN);
    }

    private void updatePresentation(EnumType.Presentation presentation) {
        updateType(currentType().withPresentation(presentation));
    }
}