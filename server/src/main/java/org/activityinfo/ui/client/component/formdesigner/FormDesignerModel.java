package org.activityinfo.ui.client.component.formdesigner;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/21/2015.
 */
public class FormDesignerModel {

    private final BiMap<ResourceId, FormClass> formFieldToSubFormClass = HashBiMap.create();

    private final FormClass rootFormClass;

    public FormDesignerModel(FormClass rootFormClass) {
        Preconditions.checkNotNull(rootFormClass);
        this.rootFormClass = rootFormClass;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public FormClass registerNewSubform(ResourceId formFieldId) {
        final FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(rootFormClass.getId());
        registerSubform(formFieldId, formClass);
        return formClass;
    }

    public FormDesignerModel registerSubform(ResourceId formFieldId, FormClass formClass) {
        Preconditions.checkNotNull(formFieldId);
        Preconditions.checkNotNull(formClass);

        formFieldToSubFormClass.put(formFieldId, formClass);
        return this;
    }

    public FormClass getSubform(ResourceId formFieldId) {
        return formFieldToSubFormClass.get(formFieldId);
    }

    public FormElementContainer getElementContainer(ResourceId resourceId) {
        return getRootFormClass().getElementContainer(resourceId);
    }

    public void updateFieldOrder(FormDesignerPanel formDesignerPanel) {

        Map<ResourceId, FormField> fieldMap = Maps.newHashMap();
        for (FormField field : rootFormClass.getFields()) {
            fieldMap.put(field.getId(), field);
        }

        // update the order of the model
        List<FormElement> elements = Lists.newArrayList();
        FlowPanel panel = formDesignerPanel.getDropPanel();
        for (int i = 0; i != panel.getWidgetCount(); ++i) {
            Widget widget = panel.getWidget(i);
            String fieldId = widget.getElement().getAttribute(FormDesignerConstants.DATA_FIELD_ID);
            elements.add(fieldMap.get(ResourceId.valueOf(fieldId)));
        }

        rootFormClass.getElements().clear();
        rootFormClass.getElements().addAll(elements);
    }

    public void removeSubform(FormClass subForm) {
        ResourceId formFieldId = formFieldToSubFormClass.inverse().get(subForm);
        formFieldToSubFormClass.remove(formFieldId);
        rootFormClass.removeField(formFieldId);
    }
}
