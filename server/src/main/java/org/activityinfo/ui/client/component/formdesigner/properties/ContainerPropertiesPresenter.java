package org.activityinfo.ui.client.component.formdesigner.properties;
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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElementContainer;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.model.type.subform.SubFormTypeRegistry;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/15/2015.
 */
public class ContainerPropertiesPresenter {

    private final FormDesigner formDesigner;
    private final ContainerPropertiesPanel view;

    private HandlerRegistration labelKeyUpHandler;
    private HandlerRegistration subformKindChangeHandler;
    private HandlerRegistration subformSubKindChangeHandler;

    private final Map<String, FormInstance> kindIdToInstance = Maps.newHashMap();

    public ContainerPropertiesPresenter(@Nonnull FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.view = formDesigner.getFormDesignerPanel().getContainerPropertiesPanel();
    }

    public void show(@Nonnull final FieldsHolder fieldsHolder) {
        if (fieldsHolder.equals(formDesigner.getModel().getSelectedWidgetContainer())) {
            return; // skip, container is already selected
        }

        reset();

        view.getLabelGroup().setVisible(true);
        view.getLabel().setValue(Strings.nullToEmpty(fieldsHolder.getElementContainer().getLabel()));

        validateLabel();

        labelKeyUpHandler = view.getLabel().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (validateLabel()) {
                    fieldsHolder.getElementContainer().setLabel(view.getLabel().getValue());
                    fieldsHolder.updateUi();
                }
            }
        });

        if (isSubform(fieldsHolder)) {
            view.getSubformGroup().setVisible(true);

            final FormClass subForm = (FormClass) fieldsHolder.getElementContainer();

            subformKindChangeHandler = view.getSubformType().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    subformKindChanged(view.getSubformType().getValue(view.getSubformType().getSelectedIndex()), subForm);
                }
            });

            subformSubKindChangeHandler = view.getSubformSubType().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {

                    int selectedIndex = view.getSubformSubType().getSelectedIndex();

                    if (selectedIndex != -1) {
                        FormInstance selectedInstance = kindIdToInstance.get(view.getSubformSubType().getValue(selectedIndex));
                        subForm.setSubformType(selectedInstance.getId());
                        forceSubformRerender(subForm);
                    }
                }
            });

            // kind
            ResourceId typeClassId = subForm.getSubformType().get();
            view.getSubformType().setSelectedIndex(getKindIndex(typeClassId));

            // sub type
            ClassType classType = ClassType.byDomainSilently(typeClassId.getDomain());
            if (classType == ClassType.LOCATION_TYPE) {
                view.getSubformSubTypeGroup().setVisible(true);
                initSubKindList(classType, typeClassId, subForm);
            }
        }
    }

    private boolean isSubform(FieldsHolder fieldsHolder) {
        FormElementContainer elementContainer = fieldsHolder.getElementContainer();
        return elementContainer instanceof FormClass && !elementContainer.equals(formDesigner.getRootFormClass());
    }

    private void subformKindChanged(String selectedValue, final FormClass subForm) {
        Preconditions.checkState(selectedValue != null && selectedValue.startsWith("_"),
                "Value is not valid, it must not be null and start with '_' character.");

        ResourceId kindId = ResourceId.valueOf(selectedValue);
        ClassType classType = ClassType.byId(kindId);

        view.getSubformSubTypeGroup().setVisible(classType == ClassType.LOCATION_TYPE);
        if (classType == ClassType.LOCATION_TYPE) { // for now we need sub kinds only for location types
            initSubKindList(classType, kindId, subForm);

            return;
        }

        SubFormType subformType = SubFormTypeRegistry.get().getType(selectedValue);
        if (subformType != null) {
            subForm.setSubformType(subformType.getDefinition().getId());
            forceSubformRerender(subForm);
            return;
        }

        throw new UnsupportedOperationException("Subform type is not supported, type: " + selectedValue);
    }

    private void initSubKindList(final ClassType classType, final ResourceId subKindId, final FormClass subForm) {
        view.getSubformSubType().clear();
        view.getSubformSubTypeGroup().setVisible(true);

        // restricted by activity form class (means by db of that activity but we don't want to mess code with legacy here,
        // so deal with it in QueryExecutor)
        ResourceId restrictedBy = formDesigner.getModel().getRootFormClass().getId();

        ParentCriteria criteria = ParentCriteria.isChildOf(classType.getResourceId(), restrictedBy);

        formDesigner.getResourceLocator().queryInstances(criteria).then(new Function<List<FormInstance>, Object>() {
            @Override
            public Object apply(List<FormInstance> instances) {
                int index = 0;
                for (FormInstance instance : instances) {
                    kindIdToInstance.put(instance.getId().asString(), instance);
                    view.getSubformSubType().addItem(getInstanceLabel(instance, classType.getResourceId()), instance.getId().asString());
                    if (instance.getId().equals(subKindId)) {
                        view.getSubformSubType().setSelectedIndex(index);
                    }
                    index++;
                }

                if (!instances.isEmpty()) {
                    FormInstance first = instances.iterator().next(); // on init first is selected
                    subForm.setSubformType(first.getClassId());
                    forceSubformRerender(subForm);
                }
                return null;
            }
        });
    }

    private void forceSubformRerender(FormClass subForm) {
        formDesigner.getWidgetContainer(subForm.getId()).syncWithModel(true);
    }

    private int getKindIndex(ResourceId valueId) {
        for (int i = 0; i < view.getSubformType().getItemCount(); i++) {
            if (view.getSubformType().getValue(i).equals(valueId.asString())) {
                return i;
            }
        }

        ClassType classType = ClassType.byDomainSilently(valueId.getDomain());
        if (classType != null) {
            return view.getIndexOf(classType);
        }

        throw new IllegalArgumentException("Value is unknown, value:" + valueId);
    }

    public void reset() {
        if (labelKeyUpHandler != null) {
            labelKeyUpHandler.removeHandler();
        }
        if (subformKindChangeHandler != null) {
            subformKindChangeHandler.removeHandler();
        }
        if (subformSubKindChangeHandler != null) {
            subformSubKindChangeHandler.removeHandler();
        }

        view.getLabelGroup().setVisible(false);
        view.getSubformGroup().setVisible(false);
        view.getSubformSubTypeGroup().setVisible(false);

        view.getLabel().setValue("");
    }


    private static String getInstanceLabel(FormInstance instance, ResourceId parentId) {
        if (ClassType.isClassType(parentId)) {
            return instance.getString(CuidAdapter.field(instance.getClassId(), CuidAdapter.NAME_FIELD));
        }

        String fallbackLabel = FormInstanceLabeler.getLabel(instance);
        if (Strings.isNullOrEmpty(fallbackLabel)) {
            return "no label";
        }
        return fallbackLabel;
    }

    /**
     * Returns whether code is valid.
     *
     * @return whether code is valid
     */
    private boolean validateLabel() {
        view.getLabelGroup().setShowValidationMessage(false);
        if (Strings.isNullOrEmpty(view.getLabel().getValue())) {
            view.getLabelGroup().setShowValidationMessage(true);
            return false;
        }
        return true;
    }
}

