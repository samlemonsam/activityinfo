package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formula.FormulaLexer;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

public class CalculatedTypeEditor extends TypeEditor<CalculatedFieldType> {

    interface CalculatedTypeEditorUiBinder extends UiBinder<Widget, CalculatedTypeEditor> {
    }
    private static CalculatedTypeEditorUiBinder ourUiBinder = GWT.create(CalculatedTypeEditorUiBinder.class);

    private final Widget widget;

    @UiField
    FormGroup expressionGroup;
    @UiField
    TextBox expressionTextBox;

    public CalculatedTypeEditor() {
        this.widget = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof CalculatedFieldType;
    }

    @Override
    protected void show(CalculatedFieldType type) {
        expressionTextBox.setText(type.getExpression());
        validateExpression();
    }

    @UiHandler("expressionTextBox")
    public void onExpressionChange(KeyUpEvent event) {
        validateExpression();

        updateType(currentType().withFormula(expressionTextBox.getText()));
    }

    private void validateExpression() {

        String expression = expressionTextBox.getValue();

        if(Strings.isNullOrEmpty(expression)) {
            expressionGroup.setShowValidationMessage(false);
            expressionGroup.validationStateType(ValidationStateType.NONE);
            return;
        }

        try {
            FormulaLexer lexer = new FormulaLexer(expression);
            FormulaParser parser = new FormulaParser(lexer);
            FormulaNode expr = parser.parse();
        } catch (Exception e) {
            expressionGroup.setShowValidationMessage(true);
            expressionGroup.setValidationMessage(I18N.CONSTANTS.calculationExpressionIsInvalid());
            expressionGroup.validationStateType(ValidationStateType.ERROR);
            return;
        }

        // Ok
        expressionGroup.setShowValidationMessage(false);
        expressionGroup.validationStateType(ValidationStateType.SUCCESS);
    }
}