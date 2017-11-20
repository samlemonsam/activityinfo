package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FormConverterTest {


    @Test
    public void deserializationEnumTypeNoChoices() {
        EnumType type = new EnumType(Cardinality.SINGLE, EnumType.Presentation.AUTOMATIC, Collections.<EnumItem>emptyList());
        JsonValue jsonObject = type.getParametersAsJson();

        EmbeddedEntity entity = FormConverter.toEmbeddedEntity(jsonObject);
        JsonValue fromEntity = FormConverter.fromEmbeddedEntity(entity);

        EnumType reType = EnumType.TYPE_CLASS.deserializeType(fromEntity);
        assertThat(reType.getCardinality(), equalTo(Cardinality.SINGLE));
        assertThat(reType.getPresentation(), equalTo(EnumType.Presentation.AUTOMATIC));
        assertThat(reType.getValues(), hasSize(0));
    }

}