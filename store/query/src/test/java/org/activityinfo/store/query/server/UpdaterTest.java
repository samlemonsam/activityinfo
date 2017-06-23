package org.activityinfo.store.query.server;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.spi.BlobAuthorizerStub;
import org.activityinfo.store.spi.RecordUpdate;
import org.junit.Before;
import org.junit.Test;

import static org.activityinfo.json.Json.createObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UpdaterTest {

    private Updater updater;
    private int userId = 1;

    @Before
    public void setUp() {
        MockFormCatalog catalog = new MockFormCatalog();
        updater = new Updater(catalog, userId, new BlobAuthorizerStub(), new SerialNumberProviderStub());
    }

    @Test(expected = InvalidUpdateException.class)
    public void missingChangesProperty() {
        JsonObject updateObject = createObject();
        updater.execute(updateObject);
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void invalidChangesProperty() {
        JsonObject updateObject = createObject();
        updateObject.put("changes", 42);
        updater.execute(updateObject);
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithoutClass() {
        JsonObject change = createObject();
        change.put("@id", "XYZ123-new-id");
        
        JsonArray changes = Json.createArray();
        changes.add(change);

        JsonObject updateObject = createObject();
        updateObject.put("changes", changes);
        updater.execute(updateObject);
    }

    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithInvalidClass() {
        JsonObject change = createObject();
        change.put("@id", "XYZ123");
        change.put("@class", createObject());

        JsonArray changes = Json.createArray();
        changes.add(change);

        JsonObject updateObject = createObject();
        updateObject.put("changes", changes);
        updater.execute(updateObject);
    }

    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithMissingCollection() {

        JsonObject change = createObject();
        change.put("@id", "XYZ123");
        change.put("@class", "foobar");

        JsonArray changes = Json.createArray();
        changes.add(change);

        JsonObject updateObject = createObject();
        updateObject.put("changes", changes);
        updater.execute(updateObject);
    }

    @Test
    public void missingValue() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "XYZ123");
        change.put("Q1", Json.createNull());

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertTrue(update.getChangedFieldValues().containsKey(fieldId));
    }
    
    @Test
    public void validQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "XYZ123");
        change.put("Q1", 41.3);

        RecordUpdate update = Updater.parseChange(formClass, change, userId);
        
        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }

    @Test
    public void parsedQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "XYZ123");
        change.put("Q1", "41.3");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }

    @Test(expected = InvalidUpdateException.class)
    public void invalidParsedQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "XYZ123");
        change.put("Q1", "4.1.3");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId),
            equalTo((FieldValue)new Quantity(41.3, "meters")));
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void invalidQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "XYZ123");
        change.put("Q1", "Hello world");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }

    @Test
    public void serialNumber() {
        FormClass formClass = new FormClass(ResourceId.valueOf("FORM1"));
        formClass.addField(ResourceId.valueOf("FIELD0"))
                .setType(TextType.SIMPLE)
                .setLabel("Province Code")
                .setCode("PROVINCE")
                .setRequired(true);

        FormField serialNumberField = formClass.addField(ResourceId.valueOf("FIELD1"))
                .setType(new SerialNumberType("PROVINCE", 5))
                .setRequired(true)
                .setLabel("File Number")
                .setCode("SN");

        JsonObject change = createObject();
        change.put("@id", "A");
        change.put("@class", "FORM1");
        change.put("PROVINCE", "KUNDUZ");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        updater.generateSerialNumber(formClass, serialNumberField, update);

        FieldValue serialValue = update.getChangedFieldValues().get(serialNumberField.getId());
        assertThat(serialValue, equalTo((FieldValue)new SerialNumber("KUNDUZ", 1)));

    }


}