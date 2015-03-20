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
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.model.type.subform.SubFormKind;
import org.activityinfo.model.type.subform.SubFormKindRegistry;
import org.activityinfo.model.type.subform.SubformConstants;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private HandlerRegistration subformTabCountHandler;

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

            subformKindChangeHandler = view.getSubformKind().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    subformKindChanged(view.getSubformKind().getValue(view.getSubformKind().getSelectedIndex()), subForm);
                }
            });

            subformSubKindChangeHandler = view.getSubformSubKind().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {

                    int selectedIndex = view.getSubformSubKind().getSelectedIndex();

                    if (selectedIndex != -1) {
                        final ReferenceType subFormType = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();
                        FormInstance selectedInstance = kindIdToInstance.get(view.getSubformSubKind().getValue(selectedIndex));
                        subFormType.setRange(selectedInstance.getId());
                        forceSubformRerender(subForm);
                    }
                }
            });

            subformTabCountHandler = view.getSubformTabCount().addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    final QuantityType tabCount = (QuantityType) subForm.getField(SubformConstants.TAB_COUNT_FIELD_ID).getType();
                    tabCount.setUnits(view.getSubformTabCount().getValue().toString());
                    forceSubformRerender(subForm);
                }
            });

            // kind
            ReferenceType typeClass = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();
            ResourceId typeClassId = typeClass.getRange().iterator().next();
            view.getSubformKind().setSelectedIndex(getKindIndex(typeClassId));

            // sub kind
            ClassType classType = ClassType.byDomainSilently(typeClassId.getDomain());
            if (classType == ClassType.LOCATION_TYPE) {
                view.getSubformSubKindGroup().setVisible(true);
                initSubKindList(classType, typeClassId, subForm);
            }

            // tabs count
            QuantityType tabCount = (QuantityType) subForm.getField(SubformConstants.TAB_COUNT_FIELD_ID).getType();
            view.getSubformTabCount().setValue(Double.parseDouble(tabCount.getUnits()));
        }
    }

    private boolean isSubform(FieldsHolder fieldsHolder) {
        FormElementContainer elementContainer = fieldsHolder.getElementContainer();
        return elementContainer instanceof FormClass && !elementContainer.equals(formDesigner.getRootFormClass());
    }

    private void subformKindChanged(String selectedValue, final FormClass subForm) {
        Preconditions.checkState(selectedValue != null && selectedValue.startsWith("_"),
                "Value is not valid, it must not be null and start with '_' character.");

        final ReferenceType subFormType = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();

        ResourceId kindId = ResourceId.valueOf(selectedValue);
        ClassType classType = ClassType.byId(kindId);

        view.getSubformSubKindGroup().setVisible(classType == ClassType.LOCATION_TYPE);
        if (classType == ClassType.LOCATION_TYPE) { // for now we need sub kinds only for location types
            initSubKindList(classType, kindId, subForm);

            return;
        }

        SubFormKind kind = SubFormKindRegistry.get().getKind(selectedValue);
        if (kind != null) {
            subFormType.setRange(kind.getDefinition().getId());
            forceSubformRerender(subForm);
            return;
        }

        throw new UnsupportedOperationException("Subform type is not supported, type: " + selectedValue);
    }

    private void initSubKindList(final ClassType classType, final ResourceId subKindId, final FormClass subForm) {
        view.getSubformSubKind().clear();
        view.getSubformSubKindGroup().setVisible(true);

        // restricted by activity form class (means by db of that activity but we don't want to mess code with legacy here,
        // so deal with it in QueryExecutor)
        ResourceId restrictedBy = formDesigner.getModel().getRootFormClass().getId();

        ParentCriteria criteria = ParentCriteria.isChildOf(classType.getResourceId(), restrictedBy);
        final ReferenceType subFormType = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();

        formDesigner.getResourceLocator().queryInstances(criteria).then(new Function<List<FormInstance>, Object>() {
            @Nullable
            @Override
            public Object apply(List<FormInstance> instances) {
                int index = 0;
                for (FormInstance instance : instances) {
                    kindIdToInstance.put(instance.getId().asString(), instance);
                    view.getSubformSubKind().addItem(getInstanceLabel(instance, classType.getResourceId()), instance.getId().asString());
                    if (instance.getId().equals(subKindId)) {
                        view.getSubformSubKind().setSelectedIndex(index);
                    }
                    index++;
                }

                if (!instances.isEmpty()) {
                    FormInstance first = instances.iterator().next(); // on init first is selected
                    subFormType.setRange(first.getClassId());
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
        for (int i = 0; i < view.getSubformKind().getItemCount(); i++) {
            if (view.getSubformKind().getValue(i).equals(valueId.asString())) {
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
        if (subformTabCountHandler != null) {
            subformTabCountHandler.removeHandler();
        }
        view.getLabelGroup().setVisible(false);
        view.getSubformGroup().setVisible(false);
        view.getSubformSubKindGroup().setVisible(false);
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

