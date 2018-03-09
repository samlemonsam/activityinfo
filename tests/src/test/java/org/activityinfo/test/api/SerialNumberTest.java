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
package org.activityinfo.test.api;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that file number fields work correctly.
 */
public class SerialNumberTest {

    private ApiTestHarness harness = new ApiTestHarness();

    @Test
    public void simpleTest() {

        TestDatabase database = harness.createDatabase();
        ActivityInfoClient client = harness.client();

        FormClass formClass = new FormClass(harness.newFormId());
        formClass.setLabel("Test Form");
        formClass.setDatabaseId(database.getId());

        FormField partnerField = CuidAdapter.partnerField(formClass);
        formClass.addElement(partnerField);

        FormField fileNumberField = formClass.addField(harness.newFieldId())
            .setType(new SerialNumberType())
            .setLabel("Record Number");

        FormField nameField = formClass.addField(harness.newFieldId())
            .setType(TextType.SIMPLE)
            .setLabel("Name");

        client.createForm(formClass);

        FormInstance record1 = new FormInstance(harness.newRecordId(), formClass.getId());
        record1.set(nameField.getId(), "Sue");
        record1.set(partnerField.getId(), database.getDefaultPartner());

        FormInstance record2 = new FormInstance(harness.newRecordId(), formClass.getId());
        record2.set(nameField.getId(), "Bob");
        record2.set(partnerField.getId(), database.getDefaultPartner());

        client.createRecord(record1);
        client.createRecord(record2);

        record1 = client.getTypedRecord(formClass, record1.getId());
        record2 = client.getTypedRecord(formClass, record2.getId());

        assertThat(record1.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber(1)));
        assertThat(record2.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber(2)));
    }


    @Test
    public void testWithPrefix() {

        TestDatabase database = harness.createDatabase();
        ActivityInfoClient client = harness.client();

        FormClass formClass = new FormClass(harness.newFormId());
        formClass.setLabel("Test Prefix");
        formClass.setDatabaseId(database.getId());

        FormField partnerField = CuidAdapter.partnerField(formClass);
        formClass.addElement(partnerField);

        FormField provinceField = formClass.addField(harness.newFieldId())
            .setType(TextType.SIMPLE)
            .setLabel("Province Code")
            .setCode("PROVINCE")
            .setRequired(true);

        FormField fileNumberField = formClass.addField(harness.newFieldId())
            .setCode("SN")
            .setType(new SerialNumberType("PROVINCE", 5))
            .setLabel("Record Number");

        client.createForm(formClass);

        FormInstance kunduz1 = new FormInstance(harness.newRecordId(), formClass.getId());
        kunduz1.set(provinceField.getId(), "KUNDUZ");
        kunduz1.set(partnerField.getId(), database.getDefaultPartner());
        client.createRecord(kunduz1);

        FormInstance kunduz2 = new FormInstance(harness.newRecordId(), formClass.getId());
        kunduz2.set(provinceField.getId(), "KUNDUZ");
        kunduz2.set(partnerField.getId(), database.getDefaultPartner());
        client.createRecord(kunduz2);

        FormInstance takhar = new FormInstance(harness.newRecordId(), formClass.getId());
        takhar.set(provinceField.getId(), "TAKHAR");
        takhar.set(partnerField.getId(), database.getDefaultPartner());
        client.createRecord(takhar);

        // Verify that the serial numbers have been assigned
        kunduz1 = client.getTypedRecord(formClass, kunduz1.getId());
        kunduz2 = client.getTypedRecord(formClass, kunduz2.getId());
        takhar = client.getTypedRecord(formClass, takhar.getId());

        assertThat(kunduz1.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber("KUNDUZ", 1)));
        assertThat(kunduz2.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber("KUNDUZ", 2)));
        assertThat(takhar.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber("TAKHAR", 1)));

        // Now verify that we can query a table with serial numbers

        QueryModel queryModel = new QueryModel(formClass.getId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField("SN");

        ColumnSet columnSet = client.queryTable(queryModel);

        ColumnView sn = columnSet.getColumnView("SN");
    }
}
