package org.activityinfo.test;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that file number fields work correctly.
 */
public class SerialNumberTest {

    private ActivityInfoClient client;

    private ReferenceValue partnerRef;
    private ResourceId databaseId;

    @Before
    public void setupForm() {
        client = new ActivityInfoClient("http://localhost:8080/", "akbertram@gmail.com", "dfdf");
        databaseId = client.createDatabase("Test Db");
        partnerRef = CuidAdapter.partnerRef(CuidAdapter.getLegacyIdFromCuid(databaseId), 2156);
    }

    @Test
    public void simpleTest() {

        FormClass formClass = new FormClass(CuidAdapter.generateActivityId());
        formClass.setLabel("Test Form");
        formClass.setDatabaseId(databaseId);

        FormField partnerField = CuidAdapter.partnerField(formClass);
        formClass.addElement(partnerField);

        FormField fileNumberField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setType(new SerialNumberType())
                .setLabel("Record Number");

        FormField nameField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setType(TextType.SIMPLE)
                .setLabel("Name");

        client.createForm(formClass);

        FormInstance record1 = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        record1.set(nameField.getId(), "Sue");
        record1.set(partnerField.getId(), partnerRef);

        FormInstance record2 = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        record2.set(nameField.getId(), "Bob");
        record2.set(partnerField.getId(), partnerRef);

        client.createRecord(record1);
        client.createRecord(record2);

        record1 = client.getTypedRecord(formClass, record1.getId());
        record2 = client.getTypedRecord(formClass, record2.getId());

        assertThat(record1.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber(1)));
        assertThat(record2.get(fileNumberField.getId()), equalTo((FieldValue) new SerialNumber(2)));
    }

    @Test
    public void testWithPrefix() {

        FormClass formClass = new FormClass(CuidAdapter.generateActivityId());
        formClass.setLabel("Test Prefix");
        formClass.setDatabaseId(databaseId);

        FormField partnerField = CuidAdapter.partnerField(formClass);
        formClass.addElement(partnerField);

        FormField provinceField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setType(TextType.SIMPLE)
                .setLabel("Province Code")
                .setCode("PROVINCE")
                .setRequired(true);

        FormField fileNumberField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setCode("SN")
                .setType(new SerialNumberType("PROVINCE", 5))
                .setLabel("Record Number");

        client.createForm(formClass);

        FormInstance kunduz1 = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        kunduz1.set(provinceField.getId(), "KUNDUZ");
        kunduz1.set(partnerField.getId(), partnerRef);
        client.createRecord(kunduz1);

        FormInstance kunduz2 = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        kunduz2.set(provinceField.getId(), "KUNDUZ");
        kunduz2.set(partnerField.getId(), partnerRef);
        client.createRecord(kunduz2);

        FormInstance takhar = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        takhar.set(provinceField.getId(), "TAKHAR");
        takhar.set(partnerField.getId(), partnerRef);
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
