package org.activityinfo.model.type.enumerated;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.type.Cardinality;
import org.junit.Test;

import java.util.Collections;

import static org.activityinfo.json.Json.createObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EnumTypeTest {

    @Test
    public void deserializationCheckboxes() {

        JsonObject object = createObject();
        object.put("presentation", "CHECKBOX");
        object.put("cardinality", "SINGLE");
        object.put("values", Json.createArray());

        EnumType enumType = EnumType.TYPE_CLASS.deserializeType(object);
        assertThat(enumType.getPresentation(), equalTo(EnumType.Presentation.RADIO_BUTTON));
    }


    @Test
    public void deserializationEnumTypeNoChoices() {
        EnumType type = new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList());
        org.activityinfo.json.JsonObject jsonObject = type.getParametersAsJson();

        EnumType reType = EnumType.TYPE_CLASS.deserializeType(jsonObject);
        assertThat(reType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(reType.getPresentation(), equalTo(EnumType.Presentation.AUTOMATIC));
        assertThat(reType.getValues(), hasSize(0));
    }
}