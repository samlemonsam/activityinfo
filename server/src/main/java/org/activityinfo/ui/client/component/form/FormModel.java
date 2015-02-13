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

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.promise.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 02/13/2015.
 */
public class FormModel {

    private final ResourceLocator locator;
    private final BiMap<ResourceId, FormClass> formFieldToSubFormClass = HashBiMap.create();

    private FormClass rootFormClass;

    // validation form class is used to refer to "top-level" form class.
    // For example "Properties panel" renders current type-formClass but in order to validate expression we need
    // reference to formClass that is currently editing on FormDesigner.
    // it can be null.
    private FormClass validationFormClass = null;

    public FormModel(ResourceLocator locator) {
        this.locator = locator;
    }

    public Promise<Void> loadFormClassWithDependentSubForms(ResourceId rootClassId) {
        return locator.getFormClass(rootClassId).then(new Function<FormClass, FormClass>() {
            @Nullable
            @Override
            public FormClass apply(@Nullable FormClass input) {
                FormModel.this.rootFormClass = input;
                return input;
            }
        }).join(new Function<FormClass, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable FormClass rootFormClass) {
                List<Promise<FormClass>> promises = Lists.newArrayList();
                for (final FormField formField : rootFormClass.getFields()) {
                    if (formField.getType() instanceof SubFormType) {
                        SubFormType subFormType = (SubFormType) formField.getType();
                        Promise<FormClass> promise = locator.getFormClass(subFormType.getClassId());
                        promise.then(new Function<FormClass, Object>() {
                            @Nullable
                            @Override
                            public Object apply(@Nullable FormClass subForm) {
                                formFieldToSubFormClass.put(formField.getId(), subForm);
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

    public List<FormField> getAllFormsFields() {
        List<FormField> formFields = Lists.newArrayList(getRootFormClass().getFields());
        for (FormClass subForm : formFieldToSubFormClass.values()) {
            formFields.addAll(subForm.getFields());
        }
        return formFields;
    }

    public FormClass getSubFormByFormFieldId(ResourceId formFieldId) {
        return formFieldToSubFormClass.get(formFieldId);
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public void setRootFormClass(@Nonnull FormClass rootFormClass) {
        this.rootFormClass = rootFormClass;
    }

    public FormClass getValidationFormClass() {
        return validationFormClass;
    }

    public void setValidationFormClass(FormClass validationFormClass) {
        this.validationFormClass = validationFormClass;
    }
}
