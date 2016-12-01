package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormCatalog;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UpdaterTest {

    private Updater updater;
    private int userId = 1;

    @Before
    public void setUp() {
        MockFormCatalog catalog = new MockFormCatalog();
        updater = new Updater(catalog, userId);
    }

    @Test(expected = InvalidUpdateException.class)
    public void missingChangesProperty() {
        JsonObject updateObject = new JsonObject();
        updater.execute(updateObject);
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void invalidChangesProperty() {
        JsonObject updateObject = new JsonObject();
        updateObject.addProperty("changes", 42);
        updater.execute(updateObject);
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithoutClass() {
        JsonObject change = new JsonObject();
        change.addProperty("@id", "XYZ123-new-id");
        
        JsonArray changes = new JsonArray();
        changes.add(change);
        
        JsonObject updateObject = new JsonObject();
        updateObject.add("changes", changes);
        updater.execute(updateObject);
    }

    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithInvalidClass() {
        JsonObject change = new JsonObject();
        change.addProperty("@id", "XYZ123");
        change.add("@class", new JsonObject());

        JsonArray changes = new JsonArray();
        changes.add(change);

        JsonObject updateObject = new JsonObject();
        updateObject.add("changes", changes);
        updater.execute(updateObject);
    }

    @Test(expected = InvalidUpdateException.class)
    public void newResourceWithMissingCollection() {

        JsonObject change = new JsonObject();
        change.addProperty("@id", "XYZ123");
        change.addProperty("@class", "foobar");

        JsonArray changes = new JsonArray();
        changes.add(change);

        JsonObject updateObject = new JsonObject();
        updateObject.add("changes", changes);
        updater.execute(updateObject);
    }

    @Test
    public void missingValue() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = new JsonObject();
        change.addProperty("@id", "A");
        change.addProperty("@class", "XYZ123");
        change.add("Q1", JsonNull.INSTANCE);

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertTrue(update.getChangedFieldValues().containsKey(fieldId));
    }
    
    @Test
    public void validQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));
   
        JsonObject change = new JsonObject();
        change.addProperty("@id", "A");
        change.addProperty("@class", "XYZ123");
        change.addProperty("Q1", 41.3);

        RecordUpdate update = Updater.parseChange(formClass, change, userId);
        
        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }

    @Test
    public void parsedQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = new JsonObject();
        change.addProperty("@id", "A");
        change.addProperty("@class", "XYZ123");
        change.addProperty("Q1", "41.3");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }

    @Test(expected = InvalidUpdateException.class)
    public void invalidParsedQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = new JsonObject();
        change.addProperty("@id", "A");
        change.addProperty("@class", "XYZ123");
        change.addProperty("Q1", "4.1.3");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }
    
    @Test(expected = InvalidUpdateException.class)
    public void invalidQuantity() {
        ResourceId fieldId = ResourceId.valueOf("Q1");
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ123"));
        formClass.addElement(new FormField(fieldId).setType(new QuantityType("meters")));

        JsonObject change = new JsonObject();
        change.addProperty("@id", "A");
        change.addProperty("@class", "XYZ123");
        change.addProperty("Q1", "Hello world");

        RecordUpdate update = Updater.parseChange(formClass, change, userId);

        assertThat(update.getChangedFieldValues().get(fieldId), equalTo((FieldValue)new Quantity(41.3, "meters")));
    }


    private FormCatalog emptyCatalog() {
        FormCatalog catalog = EasyMock.createMock(FormCatalog.class);
        expect(catalog.lookupForm(EasyMock.<ResourceId>anyObject())).andReturn(Optional.<FormAccessor>absent());
        expect(catalog.getForm(EasyMock.<ResourceId>anyObject())).andReturn(Optional.<FormAccessor>absent());
        replay(catalog);
        return catalog;
    }

}