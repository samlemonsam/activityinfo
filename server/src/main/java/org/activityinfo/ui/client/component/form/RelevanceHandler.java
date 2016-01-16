package org.activityinfo.ui.client.component.form;
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
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.expr.ExprLexer;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormField;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 7/30/14.
 */
public class RelevanceHandler {

    private final SimpleFormPanel simpleFormPanel;
    private List<FormField> fieldsWithSkipExpression = Lists.newArrayList();
    private Set<FieldContainer> fieldsWithAppliedRelevance = Sets.newHashSet();

    public RelevanceHandler(SimpleFormPanel simpleFormPanel) {
        this.simpleFormPanel = simpleFormPanel;
    }

    public void onValueChange() {
        for (FormField formField : fieldsWithSkipExpression) {
            applyRelevanceLogic(formField);
        }
    }

    private void applyRelevanceLogic(final FormField field) {
        if (field.hasRelevanceConditionExpression()) {
            try {
                ExprLexer lexer = new ExprLexer(field.getRelevanceConditionExpression());
                ExprParser parser = new ExprParser(lexer);
                ExprNode expr = parser.parse();
                FieldContainer fieldContainer = simpleFormPanel.getFieldContainer(field.getId());
                if (fieldContainer != null) {
                    boolean relevant = expr.evaluateAsBoolean(new FormEvalContext(simpleFormPanel.getFormClass(), simpleFormPanel.getInstance()));
                    fieldContainer.getFieldWidget().setReadOnly(!relevant);

                    if (!relevant) {
                        if (resettingValues) { // we are in resetting state -> handle nested relevance
                            fieldContainer.getFieldWidget().clearValue();
                            fieldContainer.getFieldWidget().fireValueChanged();
                        }
                        fieldsWithAppliedRelevance.add(fieldContainer);
                    } else {
                        fieldsWithAppliedRelevance.remove(fieldContainer);
                    }
                } else {
                    Log.error("Can't find container for fieldId: " + field.getId() + ", fieldName: " + field.getLabel() + ", expression: " + field.getRelevanceConditionExpression());
                }
            } catch (Exception e) {
                Log.error("Error: Unable to apply relevance logic. FieldId: " + field.getId() +
                        ", fieldName: " + field.getLabel() + ", expression: " + field.getRelevanceConditionExpression(), e);
            }
        }
    }

    private boolean resettingValues = false;

    public void resetValuesForFieldsWithAppliedRelevance() {
        try {
            resettingValues = true;
            for (FieldContainer container : fieldsWithAppliedRelevance) {
                container.getFieldWidget().clearValue();
                container.getFieldWidget().fireValueChanged();
            }
        } finally {
            resettingValues = false;
        }
    }

    public void formClassChanged() {
        fieldsWithSkipExpression = Lists.newArrayList();

        for (FormField formField : simpleFormPanel.getFormClass().getFields()) {
            if (formField.hasRelevanceConditionExpression()) {
                fieldsWithSkipExpression.add(formField);
            }
        }
    }
}
