package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

/**
 * Allows the user to edit a serial number field
 */
public class SerialNumberTypeEditor implements IsWidget {



    interface SerialNumberTypeEditorUiBinder extends UiBinder<FlowPanel, SerialNumberTypeEditor> {
    }

    private static SerialNumberTypeEditorUiBinder ourUiBinder = GWT.create(SerialNumberTypeEditorUiBinder.class);


    private final FlowPanel panel;


    @UiField
    TextBox prefixFormulaBox;

    @UiField
    FormGroup prefixGroup;

    private FieldWidgetContainer currentField;

    public SerialNumberTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void show(FieldWidgetContainer container) {
        if(container.getFormField().getType() instanceof SerialNumberType) {
            this.currentField = container;

            SerialNumberType type = (SerialNumberType) currentField.getFormField().getType();
            prefixFormulaBox.setText(type.getPrefixFormula());
            panel.setVisible(true);
            validateFormula(type.getPrefixFormula());

        } else {
            panel.setVisible(false);
        }
    }

    @UiHandler("prefixFormulaBox")
    public void onFormulaChange(KeyUpEvent event) {

        String updatedFormula = prefixFormulaBox.getValue();

        SerialNumberType type = (SerialNumberType) currentField.getFormField().getType();
        SerialNumberType updatedType = type.withPrefixFormula(updatedFormula);

        currentField.getFormField().setType(updatedType);

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
