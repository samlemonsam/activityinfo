package org.activityinfo.model.type.enumerated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class EnumTypeTest {

    @Test
    public void deserializationCheckboxes() {

        JsonObject object = new JsonObject();
        object.addProperty("presentation", "CHECKBOX");
        object.addProperty("cardinality", "SINGLE");
        object.add("values", new JsonArray());

        EnumType enumType = EnumType.TYPE_CLASS.deserializeType(object);
        assertThat(enumType.getPresentation(), equalTo(EnumType.Presentation.RADIO_BUTTON));
    }

}