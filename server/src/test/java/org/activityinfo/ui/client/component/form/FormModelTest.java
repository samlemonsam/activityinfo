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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.form.subform.SubFormInstanceLoader;
import org.activityinfo.ui.client.dispatch.state.GxtStateProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author yuriyz on 02/19/2015.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class FormModelTest extends CommandTestCase2 {

    private static final ResourceId DATABASE_ID = CuidAdapter.databaseId(1);
    
    private FormClass masterFormClass;
    private FormClass subFormClass;
    private FormField subFormChildField;
    private FormField subFormField;

    @Test
    public void modelState() {
        
        setupForms();
        
        FormModel formModel = new FormModel(locator, new GxtStateProvider());
        assertResolves(formModel.loadFormClassWithDependentSubForms(masterFormClass.getId()));

        assertEquals(formModel.getRootFormClass().getId(), masterFormClass.getId());
        assertEquals(formModel.getRootFormClass().getDatabaseId(), masterFormClass.getDatabaseId());

        assertNotNull(formModel.getSubFormByOwnerFieldId(subFormField.getId()));
        assertNotNull(formModel.getClassByField(subFormChildField.getId()));
    }

    @Ignore
    @Test
    public void doNotPersistFormClassWithStaleSubformReference() {
        
        setupForms();
        
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setDatabaseId(DATABASE_ID);

        FormClass subform = new FormClass(ResourceId.generateId());
        subform.setDatabaseId(DATABASE_ID);

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

    private FormModel newFormModel() {
        FormModel model = new FormModel(locator, new GxtStateProvider());
        model.put(masterFormClass);
        model.putSubform(subFormField.getId(), subFormClass);
        return model;
    }

    @Test
    public void subformInstancesPersistence() {
        
        setupForms();

        FormInstance rootInstance = new FormInstance(ResourceId.generateSubmissionId(masterFormClass), masterFormClass.getId());
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.PARTNER_FIELD),
                new ReferenceValue(
                        new RecordRef(
                            CuidAdapter.partnerFormId(1),
                            CuidAdapter.partnerRecordId(1))));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(
                        new RecordRef(
                                CuidAdapter.locationFormClass(1),
                                CuidAdapter.locationInstanceId(1))));
        
        FormModel formModel = newFormModel();
        formModel.setWorkingRootInstance(rootInstance);

        String tab1 = new Month(2015, 3).toString();
        String tab2 = new Month(2015, 8).toString();

        // Tab1
        FormInstance valueInstance1 = formModel.getWorkingInstance(subFormChildField.getId(), tab1).get();
        valueInstance1.set(subFormChildField.getId(), TextValue.valueOf("tab1"));

        // Tab2
        FormInstance valueInstance2 = formModel.getWorkingInstance(subFormChildField.getId(), tab2).get();
        valueInstance2.set(subFormChildField.getId(), TextValue.valueOf("tab2"));

        formModel.getChangedInstances().add(valueInstance1);
        formModel.getChangedInstances().add(valueInstance2);

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

        formModel.getChangedInstances().add(valueInstance1);
        formModel.getChangedInstances().add(valueInstance2);

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

        assertEquals(1, loadedInstances.size());
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
        masterFormClass.setLabel("Master Form");
        masterFormClass.setDatabaseId(CuidAdapter.databaseId(1));

        FormField partnerField = new FormField(CuidAdapter.partnerField(3));
        partnerField.setLabel("Partner");
        partnerField.setType(ReferenceType.single(CuidAdapter.partnerFormId(1)));
        masterFormClass.addElement(partnerField);

        FormField labelField = masterFormClass.addField(CuidAdapter.generateIndicatorId());
        labelField.setLabel("label1");
        labelField.setType(TextType.SIMPLE);

        subFormClass = new FormClass(ResourceId.generateId());
        subFormClass.setDatabaseId(masterFormClass.getDatabaseId());
        subFormClass.setParentFormId(masterFormId);
        subFormClass.setSubFormKind(SubFormKind.MONTHLY);
        subFormChildField = subFormClass.addField();
        subFormChildField.setType(TextType.SIMPLE);
        subFormChildField.setLabel("Child field");

        subFormField = masterFormClass.addField(CuidAdapter.generateIndicatorId());
        subFormField.setType(new SubFormReferenceType(subFormClass.getId()));
        subFormField.setLabel("Sub form");

        assertResolves(locator.persist(subFormClass));
        assertResolves(locator.persist(masterFormClass));
        
    }
}
