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
package org.activityinfo.ui.client.component.formdesigner;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FocusPanel;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.container.FieldPanel;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/21/2015.
 */
public class FormDesignerModel {

    private final BiMap<ResourceId, FormClass> formFieldToSubFormClass = HashBiMap.create();

    private final Map<FocusPanel, FieldPanel> focusMap = Maps.newHashMap();
    private final FormClass rootFormClass;
    private WidgetContainer selectedWidgetContainer;

    public FormDesignerModel(FormClass rootFormClass) {
        Preconditions.checkNotNull(rootFormClass);
        this.rootFormClass = rootFormClass;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public FormClass registerNewSubform(ResourceId formFieldId, SubFormKind subFormKind) {
        final FormClass formClass = new FormClass(ResourceId.generateId());

        formClass.setDatabaseId(rootFormClass.getDatabaseId());
        formClass.setSubFormKind(subFormKind);

        registerSubform(formFieldId, formClass);
        return formClass;
    }

    public List<FormClass> getSubforms() {
        return Lists.newArrayList(formFieldToSubFormClass.values());
    }

    public boolean isSubform(@Nullable ResourceId subformId) {
        if (subformId != null) {
            for (FormClass subform : formFieldToSubFormClass.values()) {
                if (subform.getId().equals(subformId)) {
                    return true;
                }
            }
        }
        return false;
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

    public FormClass getFormClass(ResourceId formClassId) {
        return (FormClass) getElementContainer(formClassId);
    }

    public FormField getFieldById(ResourceId fieldId) {
        return getFormClassByElementId(fieldId).getField(fieldId);
    }

    public FormClass getFormClassByElementId(ResourceId elementId) {
        FormElementContainer rootContainer = getRootFormClass().getElementContainer(elementId); // try root first
        if (rootContainer != null) {
            return getRootFormClass();
        }
        Optional<FormElement> fromRoot = getRootFormClass().getElement(elementId); // try root first
        if (fromRoot.isPresent()) {
            return getRootFormClass();
        }
        for (FormClass subform : formFieldToSubFormClass.values()) {
            if (subform.getId().equals(elementId)) {
                return subform;
            }
            FormElementContainer fromSubform = subform.getElementContainer(elementId);
            if (fromSubform != null) {
                return subform;
            }
            Optional<FormElement> subformElement = subform.getElement(elementId);
            if (subformElement.isPresent()) {
                return subform;
            }
        }
        return null;
    }

    public FormElementContainer getElementContainer(ResourceId resourceId) {
        FormElementContainer fromRoot = getRootFormClass().getElementContainer(resourceId); // try root first
        if (fromRoot != null) {
            return fromRoot;
        }
        for (FormClass subform : formFieldToSubFormClass.values()) {
            if (subform.getId().equals(resourceId)) {
                return subform;
            }
            FormElementContainer fromSubform = subform.getElementContainer(resourceId);
            if (fromSubform != null) {
                return fromSubform;
            }
        }
        return null;
    }

    public void removeSubform(FormClass subForm) {
        ResourceId formFieldId = formFieldToSubFormClass.inverse().get(subForm);
        formFieldToSubFormClass.remove(formFieldId);
        rootFormClass.removeField(formFieldId);
    }

    public FormField getSubformOwnerField(FormClass subform) {
        ResourceId ownerFieldId = formFieldToSubFormClass.inverse().get(subform);
        return rootFormClass.getField(ownerFieldId);
    }

    /**
     * Returns formfields of root formclass and all subforms.
     *
     * @return formfields of root formclass and all subforms
     */
    public List<FormField> getAllFormsFields() {
        List<FormField> formFields = Lists.newArrayList(getRootFormClass().getFields());
        for (FormClass subForm : getSubforms()) {
            formFields.addAll(subForm.getFields());
        }
        return formFields;
    }

    public void bind(EventBus eventBus) {
        eventBus.addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                selectedWidgetContainer = event.getSelectedItem();
            }
        });
    }

    public WidgetContainer getSelectedWidgetContainer() {
        return selectedWidgetContainer;
    }

    public Map<FocusPanel, FieldPanel> getFocusMap() {
        return focusMap;
    }
}
