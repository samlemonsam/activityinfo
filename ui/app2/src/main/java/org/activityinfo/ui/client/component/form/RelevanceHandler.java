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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.expr.ExprLexer;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;

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
                FieldContainer fieldContainer = simpleFormPanel.getWidgetCreator().get(field.getId());
                if (fieldContainer != null) {
                    FormModel model = simpleFormPanel.getModel();
                    Optional<FormInstance> instance = model.getWorkingInstance(field.getId(), simpleFormPanel.getSelectedKey(field));
                    FormClass formClass = model.getClassByField(field.getId());
                    boolean relevant;
                    if (instance.isPresent()) {
                        relevant = expr.evaluateAsBoolean(new FormEvalContext(formClass, instance.get()));
                    }
                    else {
                        relevant = expr.evaluateAsBoolean(new FormEvalContext(formClass, 
                                new FormInstance(ResourceId.generateSubmissionId(formClass), formClass.getId())));
                    }
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
        } catch (Exception e) {
            Log.error("Failed to reset values for fields with applied relevance");
        } finally {
            resettingValues = false;
        }
    }

    public void formClassChanged() {
        fieldsWithSkipExpression = Lists.newArrayList();

        for (FormField formField : simpleFormPanel.getModel().getAllFormsFields()) {
            if (formField.hasRelevanceConditionExpression()) {
                fieldsWithSkipExpression.add(formField);
            }
        }
    }
}
