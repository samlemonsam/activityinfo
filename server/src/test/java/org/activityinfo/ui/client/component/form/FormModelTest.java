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
import com.google.common.collect.BiMap;
import com.google.gwt.user.client.rpc.AsyncCallback;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.client.state.GxtStateProvider;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.period.PredefinedPeriods;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.form.subform.PeriodInstanceKeyedGenerator;
import org.activityinfo.ui.client.component.form.subform.SubFormInstanceLoader;
import org.activityinfo.ui.client.component.formdesigner.InstanceGeneratorTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    
    private ResourceLocatorAdaptor resourceLocator;

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
        resourceLocator = new ResourceLocatorAdaptor(getDispatcher());
    }

    @After
    public final void tearDown() {
        helper.tearDown();
    }

    @Test
    public void modelState() {
        
        setupForms();
        
        FormModel formModel = new FormModel(resourceLocator, new GxtStateProvider());
        assertResolves(formModel.loadFormClassWithDependentSubForms(masterFormClass.getId()));

        assertEquals(formModel.getRootFormClass().getId(), masterFormClass.getId());
        assertEquals(formModel.getRootFormClass().getOwnerId(), masterFormClass.getOwnerId());

        assertNotNull(formModel.getSubFormByOwnerFieldId(subFormField.getId()));
        assertNotNull(formModel.getClassByField(subFormChildField.getId()));
    }

    @Test
    public void doNotPersistFormClassWithStaleSubformReference() {
        
        setupForms();
        
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.generateId());

        FormClass subform = new FormClass(ResourceId.generateId());
        subform.setOwnerId(formClass.getId());

        FormField subformOwnerField = formClass.addField(CuidAdapter.generateIndicatorId());
        subformOwnerField.setType(new SubFormReferenceType(subform.getId()));

        resourceLocator.persist(formClass).then(new AsyncCallback<Void>() {
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

        FormModel formModel = new FormModel(resourceLocator, new GxtStateProvider());
        formModel.setWorkingRootInstance(rootInstance);
        
        Date fixedDate = InstanceGeneratorTest.fixedDate(2, 2, 2016);
        PeriodInstanceKeyedGenerator periodGenerator = periodJvmGenerator(
                subFormClass.getId());


        List<FormInstance> tabInstances = periodGenerator.generate(PredefinedPeriods.MONTHLY.getPeriod(), fixedDate, PeriodInstanceKeyedGenerator.Direction.BACK, 2);
        FormInstance tab1 = tabInstances.get(0);
        FormInstance tab2 = tabInstances.get(1);

        // Tab1
        formModel.setSelectedInstance(tab1, subFormClass);
        FormInstance valueInstance1 = formModel.getSubFormInstances().get(new FormModel.SubformValueKey(subFormClass, tab1));
        valueInstance1.set(subFormChildField.getId(), TextValue.valueOf("tab1"));

        // Tab2
        formModel.setSelectedInstance(tab2, subFormClass);
        FormInstance valueInstance2 = formModel.getSubFormInstances().get(new FormModel.SubformValueKey(subFormClass, tab2));
        valueInstance2.set(subFormChildField.getId(), TextValue.valueOf("tab2"));

        // persist all value and tab/key instances
        FormActions actions = new FormActions(resourceLocator, formModel);
        assertResolves(actions.save());

        // make sure instances are persisted
        FormInstance fetchedInstance1 = assertResolves(resourceLocator.getFormInstance(valueInstance1.getId()));
        FormInstance fetchedInstance2 = assertResolves(resourceLocator.getFormInstance(valueInstance2.getId()));

        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab1"));
        assertEquals(fetchedInstance2.get(subFormChildField.getId()), TextValue.valueOf("tab2"));

        // Update value instances

        // Tab1
        formModel.setSelectedInstance(tab1, subFormClass);
        valueInstance1 = formModel.getSubFormInstances().get(new FormModel.SubformValueKey(subFormClass, tab1));
        valueInstance1.set(subFormChildField.getId(), TextValue.valueOf("tab11"));

        // Tab2
        formModel.setSelectedInstance(tab1, subFormClass);
        valueInstance2 = formModel.getSubFormInstances().get(new FormModel.SubformValueKey(subFormClass, tab2));
        valueInstance2.set(subFormChildField.getId(), TextValue.valueOf("tab22"));

        // persist updates
        assertResolves(actions.save());

        // make sure instances are persisted
        fetchedInstance1 = assertResolves(resourceLocator.getFormInstance(valueInstance1.getId()));
        fetchedInstance2 = assertResolves(resourceLocator.getFormInstance(valueInstance2.getId()));

        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab11"));
        assertEquals(fetchedInstance2.get(subFormChildField.getId()), TextValue.valueOf("tab22"));

        // check subform loader
        FormModel emptyModel = new FormModel(resourceLocator, new GxtStateProvider());
        emptyModel.setWorkingRootInstance(rootInstance);

        // load subform instances into empty model
        assertResolves(new SubFormInstanceLoader(emptyModel).loadKeyedSubformInstances(subFormClass));
        BiMap<FormModel.SubformValueKey, FormInstance> loadedInstances = emptyModel.getSubFormInstances();

        assertEquals(loadedInstances.size(), 2);
        assertEquals(loadedInstances.get(new FormModel.SubformValueKey(subFormClass, tab1)), valueInstance1);
        assertEquals(loadedInstances.get(new FormModel.SubformValueKey(subFormClass, tab2)), valueInstance2);
    }

    private PeriodInstanceKeyedGenerator periodJvmGenerator(ResourceId subFormClassId) {
        return new PeriodInstanceKeyedGenerator(subFormClassId, new PeriodInstanceKeyedGenerator.Formatter() {
            @Override
            public String format(String pattern, Date date) {
                return new SimpleDateFormat(pattern).format(date);
            }
        }, InstanceGeneratorTest.jvmDayOfWeekProvider());
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

        assertResolves(resourceLocator.persist(subFormClass));
        assertResolves(resourceLocator.persist(masterFormClass));
        
    }
}
