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
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.server.command.CommandTestCase2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author yuriyz on 02/19/2015.
 */
@RunWith(InjectionSupport.class)
public class FormModelTest extends CommandTestCase2 {

    private ResourceLocatorAdaptor resourceLocator;

    private FormClass setupFormClass;
    private FormClass setupSubform;
    private FormField subFormChildField;
    private FormField subformOwnerField;

    @Before
    public final void setup() {
        resourceLocator = new ResourceLocatorAdaptor(getDispatcher());
        setupFormClass = persistFormClassWithSubForm();
    }

    @After
    public final void tearDown() {
        resourceLocator.remove(Lists.newArrayList(setupFormClass.getId()));
    }

    @Test
    public void modelState() {
        FormModel formModel = new FormModel(resourceLocator);
        assertResolves(formModel.loadFormClassWithDependentSubForms(setupFormClass.getId()));

        assertEquals(formModel.getRootFormClass().getId(), setupFormClass.getId());
        assertEquals(formModel.getRootFormClass().getOwnerId(), setupFormClass.getOwnerId());

        assertNotNull(formModel.getSubFormByOwnerFieldId(subformOwnerField.getId()));
        assertNotNull(formModel.getClassByField(subFormChildField.getId()));


    }


    public FormClass persistFormClassWithSubForm() {

        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.generateId());

        FormField labelField = formClass.addField();
        labelField.setLabel("label1");
        labelField.setType(TextType.INSTANCE);

        setupSubform = new FormClass(ResourceId.generateId());
        setupSubform.setOwnerId(formClass.getId());
        subFormChildField = setupSubform.addField();
        subFormChildField.setType(TextType.INSTANCE);

        SubFormType subFormType = new SubFormType();
        subFormType.getClassReference().setRange(setupSubform.getId());

        subformOwnerField = formClass.addField();
        subformOwnerField.setType(subFormType);

        assertResolves(resourceLocator.persist(setupSubform));
        assertResolves(resourceLocator.persist(formClass));
        return formClass;
    }
}
