package org.activityinfo.test;

import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.ReferenceValue;
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
    private FormField fileNumberField;
    private FormField nameField;
    private FormClass formClass;
    private FormField partnerField;

    private ReferenceValue partnerRef;

    @Before
    public void setupForm() {

        client = new ActivityInfoClient("http://localhost:8080/", "akbertram@gmail.com", "dfdf");

        ResourceId databaseId = client.createDatabase("Test Db");

        partnerRef = CuidAdapter.partnerRef(CuidAdapter.getLegacyIdFromCuid(databaseId), 2156);

        formClass = new FormClass(CuidAdapter.generateActivityId());
        formClass.setLabel("Test Form");
        formClass.setDatabaseId(databaseId);

        partnerField = CuidAdapter.partnerField(formClass);
        formClass.addElement(partnerField);

        fileNumberField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setType(new SerialNumberType())
                .setLabel("Record Number");

        nameField = formClass.addField(CuidAdapter.generateIndicatorId())
                .setType(TextType.SIMPLE)
                .setLabel("Name");

        client.createForm(formClass);

    }

    @Test
    public void createTest() {

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
}
