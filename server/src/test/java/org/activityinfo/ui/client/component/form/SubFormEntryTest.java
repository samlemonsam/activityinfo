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

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.UpdateUserPermissions;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.dispatch.state.GxtStateProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.activityinfo.promise.PromiseMatchers.assertResolves;
import static org.junit.Assert.assertEquals;

/**
 * @author yuriyz on 02/19/2015.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class SubFormEntryTest extends CommandTestCase2 {

    private static final int OWNER = 1;
    private static final int STEFAN = 3;

    private static final int DATABASE = 1; // PEAR

    private static final int ALLOWED_PARTNER = 1;
    private static final int FORBIDDEN_PARTNER = 3;

    private static final ResourceId DATABASE_ID = CuidAdapter.databaseId(DATABASE);

    private FormClass masterFormClass;
    private FormClass subFormClass;
    private FormField subFormChildField;
    private FormField subFormField;

    private FormModel newFormModel() {
        FormModel model = new FormModel(locator, new GxtStateProvider());
        model.put(masterFormClass);
        model.putSubform(subFormField.getId(), subFormClass);
        return model;
    }

    private TypedFormRecord createRootFormRecord(int partnerId) {
        TypedFormRecord rootInstance = new TypedFormRecord(ResourceId.generateSubmissionId(masterFormClass), masterFormClass.getId());
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.START_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.END_DATE_FIELD), new LocalDate(2016,1,1));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.PARTNER_FIELD),
                new ReferenceValue(
                        new RecordRef(
                                CuidAdapter.partnerFormId(1),
                                CuidAdapter.partnerRecordId(partnerId))));
        rootInstance.set(CuidAdapter.field(masterFormClass.getId(), CuidAdapter.LOCATION_FIELD),
                new ReferenceValue(
                        new RecordRef(
                                CuidAdapter.locationFormClass(1),
                                CuidAdapter.locationInstanceId(1))));

        assertResolves(locator.persist(rootInstance));

        return rootInstance;
    }

    @Test
    public void testAllowedPartnerRecord() {
        setUser(OWNER);

        setupForms();
        setupPermissions();

        // User stefan should be allowed to create/edit/delete this record or its subform records
        TypedFormRecord allowedRootRecord = createRootFormRecord(ALLOWED_PARTNER);

        setUser(STEFAN);
        create(allowedRootRecord);
        edit(allowedRootRecord);
    }

    @Test
    public void testForbiddenPartnerRecord() {
        setUser(OWNER);

        setupForms();
        setupPermissions();

        // User stefan should NOT be allowed to create/edit/delete this record or its subform records
        TypedFormRecord forbiddenRootRecord = createRootFormRecord(FORBIDDEN_PARTNER);

        setUser(STEFAN);
        try {
            create(forbiddenRootRecord);
            throw new AssertionError("Illegal Sub-Form Record created");
        } catch (RuntimeException expected) {
            // This is expected, as the user should not be allowed to create a subform record on this parent record
            expected.getCause();
        }

        try {
            edit(forbiddenRootRecord);
            throw new AssertionError("Illegal Sub-Form Record edit");
        } catch (RuntimeException expected) {
            // This is expected, as the user should not be allowed to edit a subform record on this parent record
            expected.getCause();
        }
    }

    public void create(TypedFormRecord rootRecord) {
        FormModel formModel = newFormModel();
        formModel.setWorkingRootInstance(rootRecord);

        String tab = new Month(2015, 3).toString();
        TypedFormRecord valueInstance = formModel.getWorkingInstance(subFormChildField.getId(), tab).get();
        valueInstance.set(subFormChildField.getId(), TextValue.valueOf("tab"));
        formModel.getChangedInstances().add(valueInstance);

        // persist all value and tab/key instances
        FormActions actions = new FormActions(locator, formModel);
        assertResolves(actions.save());

        // make sure instances are persisted
        TypedFormRecord fetchedInstance = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance.getId()));
        assertEquals(fetchedInstance.get(subFormChildField.getId()), TextValue.valueOf("tab"));
    }

    public void edit(TypedFormRecord rootRecord) {
        // Set the initial record value as owner
        setUser(OWNER);

        FormModel formModel = newFormModel();
        formModel.setWorkingRootInstance(rootRecord);

        String tab = new Month(2015, 3).toString();
        TypedFormRecord valueInstance = formModel.getWorkingInstance(subFormChildField.getId(), tab).get();
        valueInstance.set(subFormChildField.getId(), TextValue.valueOf("tab"));
        formModel.getChangedInstances().add(valueInstance);

        // persist all value and tab/key instances
        FormActions actions = new FormActions(locator, formModel);
        assertResolves(actions.save());

        // make sure instances are persisted
        TypedFormRecord fetchedInstance1 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance.getId()));
        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab"));

        // Update value instances
        setUser(STEFAN);

        // Tab1
        valueInstance = formModel.getWorkingInstance(subFormChildField.getId(), tab).get();
        valueInstance.set(subFormChildField.getId(), TextValue.valueOf("tab11"));
        formModel.getChangedInstances().add(valueInstance);

        // persist updates
        assertResolves(actions.save());

        // make sure instances are persisted
        fetchedInstance1 = assertResolves(locator.getFormInstance(subFormClass.getId(), valueInstance.getId()));
        assertEquals(fetchedInstance1.get(subFormChildField.getId()), TextValue.valueOf("tab11"));
    }

    private void setupPermissions() {
        // we give Stefan rights to VIEW ALL on the database, but restrict his ability to CREATE, EDIT and DELETE to a
        // single partner (1)
        UserPermissionDTO model = new UserPermissionDTO();
        model.setName("Stefan");
        model.setEmail("stefan@irc.org");
        model.addPartner(new PartnerDTO(1,"NRC"));
        model.setAllowView(true);
        model.setAllowViewAll(true);
        model.setAllowCreate(true);
        model.setAllowEdit(true);
        model.setAllowDelete(true);

        this.execute(new UpdateUserPermissions(DATABASE, model));
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

        FormField startDateField = new FormField(CuidAdapter.field(masterFormId, CuidAdapter.START_DATE_FIELD));
        startDateField.setLabel("Start Date");
        startDateField.setType(LocalDateType.INSTANCE);
        masterFormClass.addElement(startDateField);

        FormField endDateField = new FormField(CuidAdapter.field(masterFormId, CuidAdapter.END_DATE_FIELD));
        endDateField.setLabel("End Date");
        endDateField.setType(LocalDateType.INSTANCE);
        masterFormClass.addElement(endDateField);

        FormField locationField = new FormField(CuidAdapter.field(masterFormId, CuidAdapter.LOCATION_FIELD));
        locationField.setLabel("Location");
        locationField.setType(ReferenceType.single(CuidAdapter.locationFormClass(1)));
        masterFormClass.addElement(locationField);

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
