package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprLexer;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.ui.client.widget.TextBox;
import org.activityinfo.ui.client.widget.form.FormGroup;
import org.activityinfo.ui.client.widget.form.ValidationStateType;

public class CalculatedTypeEditor implements IsWidget {


    private CalculatedFieldType currentType;

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

    public void show(FormField field) {
        if(field.getType() instanceof CalculatedFieldType) {
            currentType = (CalculatedFieldType) field.getType();
            expressionTextBox.setText(currentType.getExpression());
            validateExpression();
            widget.setVisible(true);
        } else {
            widget.setVisible(false);
        }
    }

    @UiHandler("expressionTextBox")
    public void onExpressionChange(KeyUpEvent event) {
        validateExpression();

        currentType.setExpression(expressionTextBox.getText());
    }

    private void validateExpression() {

        String expression = expressionTextBox.getValue();

        if(Strings.isNullOrEmpty(expression)) {
            expressionGroup.setShowValidationMessage(false);
            expressionGroup.validationStateType(ValidationStateType.NONE);
            return;
        }

        try {
            ExprLexer lexer = new ExprLexer(expression);
            ExprParser parser = new ExprParser(lexer);
            ExprNode expr = parser.parse();
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