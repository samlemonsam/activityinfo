package org.activityinfo.model.form;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class FormClassTest {

    @Test
    public void serializationWithMissingLabel() {
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setLabel("Form");

        FormField field = new FormField(ResourceId.generateId());
        field.setType(TextType.SIMPLE);
        formClass.addElement(field);

        JsonObject jsonObject = formClass.toJsonObject();

        FormClass reform = FormClass.fromJson(jsonObject);
        assertThat(reform.getFields(), hasSize(1));
    }
    
    @Test
    public void oldSerialization() throws IOException {
        FormClass formClass = parseResource();

        assertThat(formClass.getId(), equalTo(ResourceId.valueOf("a0000000728")));
        assertThat(formClass.getLabel(), equalTo("Youth Group Formation"));
        
        FormField partnerField = formClass.getFields().get(0);
        assertThat(partnerField.getLabel(), equalTo("Partner"));
        assertThat(partnerField.getType(), instanceOf(ReferenceType.class));
        
        ReferenceType partnerFieldType = (ReferenceType) partnerField.getType();
        assertThat(partnerFieldType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(partnerFieldType.getRange(), hasItem(ResourceId.valueOf("P0000000326")));
        
        FormField calcField = formClass.getFields().get(5);
        assertThat(calcField.getLabel(), equalTo("Number of attendees"));
        assertThat(calcField.getType(), instanceOf(CalculatedFieldType.class));
        
        CalculatedFieldType calcFieldType = (CalculatedFieldType) calcField.getType();
        assertThat(calcFieldType.getExpression(), equalTo("3250+3249"));
    }
    

    private FormClass parseResource() throws IOException {
        URL resource = Resources.getResource(FormClass.class, "OldFormClass1.json");
        String json = Resources.toString(resource, Charsets.UTF_8);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);

        return FormClass.fromJson(element.getAsJsonObject());
    }

}