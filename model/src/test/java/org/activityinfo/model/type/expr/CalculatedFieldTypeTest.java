package org.activityinfo.model.type.expr;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CalculatedFieldTypeTest {
    
    @BeforeClass
    public static void setupI18N() {
        LocaleProxy.initialize();
    }

    @Test
    public void serialization() {

        FormField field = new FormField(ResourceId.generateId());
        field.setType(new CalculatedFieldType("A+B"));

        JsonValue record = field.toJsonObject();
        System.out.println(record.toJson());

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression(), equalTo("A+B"));
    }


    @Test
    public void emptySerialization() {

        FormField field = new FormField(ResourceId.generateId());
        field.setType(new CalculatedFieldType());

        JsonValue record = field.toJsonObject();
        System.out.println(record.toJson());

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression(), nullValue());
    }
}