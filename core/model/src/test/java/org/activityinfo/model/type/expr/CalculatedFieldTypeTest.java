package org.activityinfo.model.type.expr;

import com.google.gson.JsonObject;
import net.lightoze.gwt.i18n.server.LocaleProxy;
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

        JsonObject record = field.toJsonObject();
        System.out.println(record);

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression().getExpression(), equalTo("A+B"));
    }


    @Test
    public void emptySerialization() {

        FormField field = new FormField(ResourceId.generateId());
        field.setType(new CalculatedFieldType());

        JsonObject record = field.toJsonObject();
        System.out.println(record);

        FormField read = FormField.fromJson(record);
        assertThat(read.getType(), instanceOf(CalculatedFieldType.class));

        CalculatedFieldType readType = (CalculatedFieldType) read.getType();
        assertThat(readType.getExpression(), nullValue());
    }
}