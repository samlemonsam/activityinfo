package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprParser;
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
            ExprParser.parse(updatedFormula);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
