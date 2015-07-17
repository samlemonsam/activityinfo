package org.activityinfo.model.form;

import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Test;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class FormClassTest {

    @Test
    public void serializationWithMissingLabel() {
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.ROOT_ID);
        formClass.setLabel("Form");

        FormField field = new FormField(ResourceId.generateId());
        field.setType(TextType.INSTANCE);
        formClass.addElement(field);

        Resource resource = formClass.asResource();

        FormClass reform = FormClass.fromResource(resource);
        assertThat(reform.getFields(), hasSize(1));

    }

}