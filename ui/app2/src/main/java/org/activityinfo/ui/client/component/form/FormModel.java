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
package org.activityinfo.ui.client.component.form;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1. Single form model:
 * a) definition : rootFormClass
 * b) instance level: workingRootInstance
 * <p/>
 * <p/>
 * 2. Subform model:
 * <p/>
 * a) definition:
 * rootFormClass:
 * - FormField -> SubFormType -> Sub FormClass
 *
 * subFormClass:
 * - key field - references SubFormKind (type of subformFormClass) : Collection, Partner, LocationType, Period
 * b) each subform can have multiple instances:
 * <p/>
 * b.1) valueInstance - instance that keeps values for sub form
 * valueInstance.id - generated id
 * valueInstance.classId - references subFormClass
 * valueInstance.ownerId - referenced root working instance
 * valueInstance.keyId - referenced key instance
 * <p/>
 * b.2) keyInstance - instance that keeps values for key, like period value or partner
 * keyInstance.id - generated
 * keyInstance.classId - definition class id : ClassType.getDefinition().getId();
 *
 * @author yuriyz on 02/13/2015.
 */
public class FormModel {

    /**
     * Key for subform value instances.
     */
    public static class SubformValueKey {

        private final FormClass subForm;
        private final TypedFormRecord rootInstance;

        public SubformValueKey(FormClass subForm, TypedFormRecord rootInstance) {
            this.subForm = subForm;
            this.rootInstance = rootInstance;
        }

        public FormClass getSubForm() {
            return subForm;
        }

        public TypedFormRecord getRootInstance() {
            return rootInstance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubformValueKey that = (SubformValueKey) o;

            return !(subForm != null ? !subForm.equals(that.subForm) : that.subForm != null) && !(rootInstance != null ? !rootInstance.equals(that.rootInstance) : that.rootInstance != null);

        }

        @Override
        public int hashCode() {
            int result = subForm != null ? subForm.hashCode() : 0;
            result = 31 * result + (rootInstance != null ? rootInstance.hashCode() : 0);
            return result;
        }
    }

    private final ResourceLocator locator;
    private final StateProvider stateProvider;
    private final EventBus eventBus = new SimpleEventBus();

    /**
     * FormField.SubFormType -> sub FormClass referenced in SubFormType
     */
    private final Map<ResourceId, FormClass> ownerFormFieldToSubFormClass = HashBiMap.create();

    /**
     * Keeps formfieldId to owner FormClass
     */
    private final Map<ResourceId, FormClass> formFieldToFormClass = Maps.newHashMap();

    private final Set<TypedFormRecord> persistedInstanceToRemoveByLocator = Sets.newHashSet();
    private final Set<TypedFormRecord> changedInstances = Sets.newHashSet();

    private FormClass rootFormClass;

    // validation form class is used to refer to "top-level" form class.
    // For example "Properties panel" renders current type-formClass but in order to validate expression we need
    // reference to formClass that is currently editing on FormDesigner.
    // it can be null.
    private FormClass validationFormClass = null;

    /**
     * The original, unmodified instance
     */
    private TypedFormRecord originalRootInstance;

    /**
     * A new version of the root instance, being updated by the user
     */
    private TypedFormRecord workingRootInstance;

    /**
     * Subform value instances for given Subform Class and Root instance
     */
    private final Map<SubformValueKey, Set<TypedFormRecord>> subFormInstances = Maps.newHashMap();

    /**
     * Keeps formClass to create FieldContainers map.
     */
    private final Map<ResourceId, Set<FieldContainer>> classToFields = Maps.newHashMap();

    public FormModel(ResourceLocator locator, StateProvider stateProvider) {
        this.locator = locator;
        this.stateProvider = stateProvider;
    }

    public Promise<Void> loadFormClassWithDependentSubForms(ResourceId rootClassId) {
        return locator.getFormClass(rootClassId).then(new Function<FormClass, FormClass>() {
            @Nullable
            @Override
            public FormClass apply(FormClass input) {
                FormModel.this.rootFormClass = input;
                put(input);
                return input;
            }
        }).join(new Function<FormClass, Promise<Void>>() {
            @Override
            public Promise<Void> apply(FormClass rootFormClass) {
                List<Promise<FormClass>> promises = Lists.newArrayList();
                for (final FormField formField : rootFormClass.getFields()) {
                    if (formField.getType() instanceof SubFormReferenceType) {
                        SubFormReferenceType subFormType = (SubFormReferenceType) formField.getType();
                        Promise<FormClass> promise = locator.getFormClass(subFormType.getClassId());
                        promise.then(new Function<FormClass, Object>() {
                            @Nullable
                            @Override
                            public Object apply(FormClass subForm) {
                                putSubform(formField.getId(), subForm);
                                return null;
                            }
                        });
                        promises.add(promise);
                    }
                }
                return Promise.waitAll(promises);
            }
        });
    }

    public void putSubform(ResourceId ownderFieldId, FormClass formClass) {
        ownerFormFieldToSubFormClass.put(ownderFieldId, formClass);
        put(formClass);
    }

    public void put(FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            formFieldToFormClass.put(field.getId(), formClass);
        }
    }

    public List<FormField> getAllFormsFields() {
        List<FormField> formFields = Lists.newArrayList(getRootFormClass().getFields());
        for (FormClass subForm : ownerFormFieldToSubFormClass.values()) {
            formFields.addAll(subForm.getFields());
        }
        return formFields;
    }

    public FormClass getClassByField(ResourceId formFieldId) {
        FormClass formClass = formFieldToFormClass.get(formFieldId);

        Preconditions.checkNotNull(formClass, "Unknown field, id:" + formFieldId);

        return formClass;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public FormClass getSubFormByOwnerFieldId(ResourceId formFieldId) {
        return ownerFormFieldToSubFormClass.get(formFieldId);
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public FormClass getValidationFormClass() {
        return validationFormClass;
    }

    public void setValidationFormClass(FormClass validationFormClass) {
        this.validationFormClass = validationFormClass;
    }

    public TypedFormRecord getWorkingRootInstance() {
        return workingRootInstance;
    }

    public void setWorkingRootInstance(TypedFormRecord workingRootInstance) {
        this.originalRootInstance = workingRootInstance.copy();
        this.workingRootInstance = workingRootInstance;
    }

    public Optional<TypedFormRecord> getSubformValueInstance(FormClass subformClass, TypedFormRecord rootInstance, String keyId) {
        Set<TypedFormRecord> typedFormRecords = subFormInstances.get(new SubformValueKey(subformClass, rootInstance));
        if (typedFormRecords != null) {
            for (TypedFormRecord instance : typedFormRecords) {
                if (instance.getId().asString().endsWith(keyId)) {
                    return Optional.of(instance);
                }
            }
        }
        return Optional.absent();
    }

    public Optional<TypedFormRecord> getWorkingInstance(ResourceId formFieldId, String keyId) {
        FormClass classByField = getClassByField(formFieldId);
        if (classByField.equals(rootFormClass)) {
            return Optional.of(getWorkingRootInstance());
        }
        if (classByField.isSubForm()) {
            Optional<TypedFormRecord> valueInstance = getSubformValueInstance(classByField, getWorkingRootInstance(), keyId);
            if (valueInstance.isPresent()) {
                return valueInstance;
            } else {
                TypedFormRecord newInstance = new TypedFormRecord(ResourceId.generatedPeriodSubmissionId(getWorkingRootInstance().getId(), keyId), classByField.getId());
                newInstance.setParentRecordId(getWorkingRootInstance().getId());

                SubformValueKey valueKey = new SubformValueKey(classByField, getWorkingRootInstance());
                Set<TypedFormRecord> allInstances = subFormInstances.get(valueKey);
                if (allInstances == null) {
                    allInstances = Sets.newHashSet();
                    subFormInstances.put(valueKey, allInstances);
                }
                allInstances.add(newInstance);

                return Optional.of(newInstance);
            }
        }
        throw new RuntimeException("Failed to identify working instance for field: " + formFieldId + ", keyId: " + keyId);
    }

    public Map<SubformValueKey, Set<TypedFormRecord>> getSubFormInstances() {
        return subFormInstances;
    }

    public Set<FieldContainer> getContainersOfClass(ResourceId classId) {
        Set<FieldContainer> containers = classToFields.get(classId);
        return containers != null ? containers : Collections.<FieldContainer>emptySet();
    }

    public void applyInstanceValues(TypedFormRecord instance, FormClass formClass) {
        for (FieldContainer fieldContainer : getContainersOfClass(formClass.getId())) {
            FieldValue fieldValue = instance.get(fieldContainer.getField().getId());
            if (fieldValue != null) {
                fieldContainer.getFieldWidget().setValue(fieldValue);
            } else {
                fieldContainer.getFieldWidget().clearValue();
            }
        }
    }

    public void clearFieldValues(FormClass formClass) {
        for (FieldContainer fieldContainer : getContainersOfClass(formClass.getId())) {
            fieldContainer.getFieldWidget().clearValue();
        }
    }

    public FormModel addContainerOfClass(ResourceId classId, FieldContainer fieldContainer) {
        Set<FieldContainer> containers = classToFields.get(classId);
        if (containers == null) {
            containers = Sets.newHashSet();
            classToFields.put(classId, containers);
        }
        containers.add(fieldContainer);
        return this;
    }

    public TypedFormRecord getOriginalRootInstance() {
        return originalRootInstance;
    }

    public Set<TypedFormRecord> getPersistedInstanceToRemoveByLocator() {
        return persistedInstanceToRemoveByLocator;
    }

    public StateProvider getStateProvider() {
        return stateProvider;
    }

    public Set<TypedFormRecord> getChangedInstances() {
        return changedInstances;
    }
}
