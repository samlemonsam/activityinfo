package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.common.base.Strings;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.simple.SimpleConditionList;
import org.activityinfo.model.expr.simple.SimpleConditionParser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.form.field.OptionSetProvider;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 7/24/14.
 */
public class RelevancePanelPresenter {

    private final FieldWidgetContainer fieldWidgetContainer;
    private final RelevancePanel view;
    private final OptionSetProvider optionSetProvider;

    public RelevancePanelPresenter(final FieldWidgetContainer container) {
        this.fieldWidgetContainer = container;
        this.optionSetProvider = new OptionSetProvider(container.getFormDesigner().getResourceLocator());
        view = new RelevancePanel();
        this.view.init(fieldList(container), model(container));
    }

    private List<FormField> fieldList(FieldWidgetContainer container) {

        ResourceId thisFieldId = container.getFormField().getId();
        FormClass formClass = container.getFormDesigner().getModel().getFormClassByElementId(thisFieldId);

        List<FormField> formFields = new ArrayList<>();
        for (FormField formField : formClass.getFields()) {
            if(!formField.getId().equals(thisFieldId)) {
                formFields.add(formField);
            }
        }
        return formFields;
    }

    private SimpleConditionList model(FieldWidgetContainer container) {

        String formula = container.getFormField().getRelevanceConditionExpression();
        if(Strings.isNullOrEmpty(formula)) {
            return new SimpleConditionList();
        }

        try {
            return SimpleConditionParser.parse(ExprParser.parse(formula));
        } catch (Exception e) {
            return new SimpleConditionList();
        }
    }

    public RelevancePanel getView() {
        return view;
    }

    public void updateFormField() {
        fieldWidgetContainer.getFormField().setRelevanceConditionExpression(view.build());
    }

}
