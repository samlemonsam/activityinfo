package org.activityinfo.ui.client.component.form.field;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.expr.ExprValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

import java.util.List;

/**
 * @author yuriyz on 8/14/14.
 */
public class ExprFieldWidget implements FormFieldWidget<ExprValue> {

    private static OurUiBinder uiBinder = GWT
            .create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, ExprFieldWidget> {
    }

    private final FormClass validationFormClass;

    @UiField
    HTMLPanel boxWrapper;
    @UiField
    TextBox box;
    @UiField
    SpanElement errorMessages;

    public ExprFieldWidget(FormClass validationFormClass, final ValueUpdater<ExprValue> valueUpdater) {
        uiBinder.createAndBindUi(this);

        this.validationFormClass = validationFormClass;

        this.box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                valueUpdater.update(ExprValue.valueOf(event.getValue()));
            }
        });
        this.box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                valueUpdater.update(getValue());
                validate();
            }
        });
    }

    private void validate() {
        // reset first
        boxWrapper.removeStyleName("has-error");
        errorMessages.addClassName("hide");
        try {

            ExprLexer lexer = new ExprLexer(box.getValue());
            ExprParser parser = new ExprParser(lexer);
            ExprNode expr = parser.parse();

            // expr node is created, expression is parsable
            // try to check variable names
            List<SymbolExpr> symbolExprList = Lists.newArrayList();
            gatherSymbolExprs(expr, symbolExprList);
            List<String> existingIndicatorCodes = existingFieldCodes();
            for (SymbolExpr placeholderExpr : symbolExprList) {
                if (!existingIndicatorCodes.contains(placeholderExpr.getName())) {
                    showError(I18N.MESSAGES.doesNotExist(placeholderExpr.getName()));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            // expression is invalid
            showError(I18N.CONSTANTS.calculationExpressionIsInvalid());
        }
    }

    private void showError(String errorMessage) {
        boxWrapper.addStyleName("has-error");
        errorMessages.removeClassName("hide");
        errorMessages.setInnerText(errorMessage);
    }

    private void gatherSymbolExprs(ExprNode node, List<SymbolExpr> symbolExprList) {
        if (node instanceof SymbolExpr) {
            symbolExprList.add((SymbolExpr) node);
        } else if (node instanceof FunctionCallNode) {
            FunctionCallNode functionCallNode = (FunctionCallNode) node;
            List<ExprNode> arguments = functionCallNode.getArguments();
            for (ExprNode arg : arguments) {
                gatherSymbolExprs(arg, symbolExprList);
            }
        }
    }

    private List<String> existingFieldCodes() {
        final List<String> result = Lists.newArrayList();
        if (validationFormClass != null) {
            for (FormField formField : validationFormClass.getFields()) {
                result.add(formField.getCode());
                result.add(formField.getId().asString());
            }
        }
        return result;
    }

    private ExprValue getValue() {
        return ExprValue.valueOf(box.getValue());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        box.setReadOnly(readOnly);
    }

    @Override
    public Promise<Void> setValue(ExprValue value) {
        box.setValue(value.getExpression());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return boxWrapper;
    }
}
