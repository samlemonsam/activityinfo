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
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

/**
 * Allows the user to edit a serial number field
 */
public class SerialNumberTypeEditor extends TypeEditor<SerialNumberType> {

    interface SerialNumberTypeEditorUiBinder extends UiBinder<FlowPanel, SerialNumberTypeEditor> {
    }

    private static SerialNumberTypeEditorUiBinder ourUiBinder = GWT.create(SerialNumberTypeEditorUiBinder.class);

    private final FlowPanel panel;

    @UiField
    TextBox prefixFormulaBox;

    @UiField
    FormGroup prefixGroup;

    public SerialNumberTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof SerialNumberType;
    }

    @Override
    protected void show(SerialNumberType type) {
        prefixFormulaBox.setText(type.getPrefixFormula());
        validateFormula(type.getPrefixFormula());
    }

    @UiHandler("prefixFormulaBox")
    public void onFormulaChange(KeyUpEvent event) {

        String updatedFormula = prefixFormulaBox.getValue();

        SerialNumberType updatedType = currentType().withPrefixFormula(updatedFormula);

        updateType(updatedType);

        validateFormula(updatedFormula);
    }

    private void validateFormula(String updatedFormula) {
        if(isValid(updatedFormula)) {
            prefixGroup.setShowValidationMessage(false);
            prefixGroup.validationStateType(ValidationStateType.NONE);
        } else {
            prefixGroup.setShowValidationMessage(true);
            prefixGroup.setValidationMessage(I18N.CONSTANTS.calculationExpressionIsInvalid());
        }
    }

    private boolean isValid(String updatedFormula) {
        try {
            FormulaParser.parse(updatedFormula);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
