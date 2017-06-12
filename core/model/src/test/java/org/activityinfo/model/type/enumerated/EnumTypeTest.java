package org.activityinfo.model.type.enumerated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.activityinfo.model.type.Cardinality;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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


    @Test
    public void deserializationEnumTypeNoChoices() {
        EnumType type = new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList());
        JsonObject jsonObject = type.getParametersAsJson();

        EnumType reType = EnumType.TYPE_CLASS.deserializeType(jsonObject);
        assertThat(reType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(reType.getPresentation(), equalTo(EnumType.Presentation.AUTOMATIC));
        assertThat(reType.getValues(), hasSize(0));
    }
}