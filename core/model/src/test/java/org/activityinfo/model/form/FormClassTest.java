package org.activityinfo.model.form;

import com.sun.imageio.plugins.common.I18N;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.junit.Test;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void formFieldsOrderTest() {
        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.setOwnerId(ResourceId.ROOT_ID);
        formClass.setLabel("Form");

        FormField notBuiltIn = formClass.addField(ResourceId.generateId())
                .setLabel("Built-in");

        FormField projectField = new FormField(CuidAdapter.field(formClass.getId(), CuidAdapter.PROJECT_FIELD))
                .setLabel("Project field")
                .setType(ReferenceType.single(CuidAdapter.projectFormClass(1)));
        formClass.addElement(projectField);

        FormField startDateField = new FormField(CuidAdapter.field(formClass.getId(), CuidAdapter.START_DATE_FIELD))
                .setLabel("Start date")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(startDateField);

        formClass.reorderFormFields();

        assertTrue(formClass.getFields().indexOf(projectField) == 0);
        assertTrue(formClass.getFields().indexOf(startDateField) == 1);
        assertTrue(formClass.getFields().indexOf(notBuiltIn) == 2);
    }

}