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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gwt.user.client.rpc.AsyncCallback;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.client.state.GxtStateProvider;
import org.activityinfo.model.date.Month;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.form.subform.SubFormInstanceLoader;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author yuriyz on 02/19/2015.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class FormModelTest extends CommandTestCase2 {


    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    

    private FormClass masterFormClass;
    private FormClass subFormClass;
    private FormField subFormChildField;
    private FormField subFormField;


    @BeforeClass
    public static void setUpLocale() {
        LocaleProxy.initialize();
    }


    @Before
    public final void setup() {
        helper.setUp();
    }

    @After
    public final void tearDown() {
        helper.tearDown();
    }

    @Test
    public void modelState() {
        
        setupForms();
        
        FormModel formModel = new FormModel(locator, new GxtStateProvider());
        assertResolves(formModel.loadFormClassWithDependentSubForms(masterFormClass.getId()));

        assertEquals(formModel.getRootFormClass().getId(), masterFormClass.getId());
        assertEquals(formModel.getRootFormClass().getOwnerId(), masterFormClass.getOwnerId());

        assertNotNull(formModel.getSubFormByOwnerFieldId(subFormField.getId()));
        assertNotNull(formModel.getClassByField(subFormChildField.getId()));
    }

    @Ignore
    @Test
    public void doNotPersistFormClassWithStaleSubformReference() {
        
        setupForms();
        
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.generateId());

        FormClass subform = new FormClass(ResourceId.generateId());
        subform.setOwnerId(formClass.getId());

        FormField subformOwnerField = formClass.addField(CuidAdapter.generateIndicatorId());
        subformOwnerField.setType(new SubFormReferenceType(subform.getId()));

        locator.persist(formClass).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                // expected result
            }

            @Override
            public void onSuccess(Void result) {
                throw new RuntimeException("FormClass is persisted with stale (non-existent) SubFormClass reference.");
            }
        });
    }

    @Test
    public void subformInstancesPersistence() {
        
        setupForms();

        FormInstance rootInstance = new FormInstance(ResourceId.generateSubmissionId(masterFormClass), masterFormClass.getId());
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.PARTNER_FIELD),
                new ReferenceValue(CuidAdapter.partnerInstanceId(1)));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(CuidAdapter.locationInstanceId(1)));
        
        FormModel formModel = new FormModel(locator, new GxtStateProvider());
        formModel.setWorkingRootInstance(rootInstance);

        String tab1 = new Month(2015, 3).toString();
        String tab2 = new Month(2015, 8).toString();

        // Tab1
        FormInstance valueInstance1 = formModel.getWorkingInstance(subFormChildField.getId(), tab1).get();
        valueInstance1.set(subFormChildField.getId(), TextValue.valueOf("tab1"));

        // Tab2
        FormInstance valueInstance2 = formModel.getWorkingInstance(subFormChildField.getId(), tab2).get();
        valueInstance2.set(subFormChildField.getId(), TextValue.valueOf("tab2"));

        // persist all value and tab/key instances
        FormActions actions = new FormActions(locator, formModel);
        assertResolves(actions.save());

        // make sure instances are persisted
        FormInstance fetchedInstance1 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance1.getId()));
        FormInstance fetchedInstance2 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance2.getId()));

        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab1"));
        assertEquals(fetchedInstance2.get(subFormChildField.getId()), TextValue.valueOf("tab2"));

        // Update value instances

        // Tab1
        valueInstance1 = formModel.getWorkingInstance(subFormChildField.getId(), tab1).get();
        valueInstance1.set(subFormChildField.getId(), TextValue.valueOf("tab11"));

        // Tab2
        valueInstance2 = formModel.getWorkingInstance(subFormChildField.getId(), tab2).get();
        valueInstance2.set(subFormChildField.getId(), TextValue.valueOf("tab22"));

        // persist updates
        assertResolves(actions.save());

        // make sure instances are persisted
        fetchedInstance1 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance1.getId()));
        fetchedInstance2 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance2.getId()));

        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab11"));
        assertEquals(fetchedInstance2.get(subFormChildField.getId()), TextValue.valueOf("tab22"));

        // check subform loader
        FormModel emptyModel = new FormModel(locator, new GxtStateProvider());
        emptyModel.setWorkingRootInstance(rootInstance);

        // load subform instances into empty model
        assertResolves(new SubFormInstanceLoader(emptyModel).load(subFormClass));
        Map<FormModel.SubformValueKey, Set<FormInstance>> loadedInstances = emptyModel.getSubFormInstances();

        assertEquals(loadedInstances.size(), 2);
        assertEquals(emptyModel.getSubformValueInstance(subFormClass, rootInstance, tab1).get(), valueInstance1);
        assertEquals(emptyModel.getSubformValueInstance(subFormClass, rootInstance, tab2).get(), valueInstance2);
    }

    /**
     * Sets up test fixtures. Must be called by each test to ensure that it runs AFTER
     * the dbunit setup
     */
    public void setupForms() {
        
        
        ResourceId masterFormId = CuidAdapter.activityFormClass(3);
        
        masterFormClass = new FormClass(masterFormId);
        masterFormClass.setOwnerId(CuidAdapter.databaseId(1));

        FormField labelField = masterFormClass.addField(CuidAdapter.generateIndicatorId());
        labelField.setLabel("label1");
        labelField.setType(TextType.INSTANCE);

        subFormClass = new FormClass(ResourceId.generateId());
        subFormClass.setOwnerId(masterFormClass.getId());
        subFormClass.setParentFormId(masterFormId);
        subFormChildField = subFormClass.addField();
        subFormChildField.setType(TextType.INSTANCE);

        subFormField = masterFormClass.addField(CuidAdapter.generateIndicatorId());
        subFormField.setType(new SubFormReferenceType(subFormClass.getId()));

        assertResolves(locator.persist(subFormClass));
        assertResolves(locator.persist(masterFormClass));
        
    }
}
