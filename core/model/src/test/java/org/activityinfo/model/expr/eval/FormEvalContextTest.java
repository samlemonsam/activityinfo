package org.activityinfo.model.expr.eval;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.ErrorValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.junit.Test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class FormEvalContextTest {

    @Test
    public void circularRefs() {

        FormField a = new FormField(ResourceId.generateId());
        a.setCode("A");
        a.setType(new CalculatedFieldType("B+1"));
        a.setLabel("A");

        FormField b = new FormField(ResourceId.generateId());
        b.setCode("B");
        b.setType(new CalculatedFieldType("A/50"));
        b.setLabel("B");

        FormClass formClass = new FormClass(ResourceId.generateId());
        formClass.addElement(a);
        formClass.addElement(b);

        FormEvalContext context = new FormEvalContext(formClass);
        context.setInstance(new FormInstance(ResourceId.generateId(), formClass.getId()));

        assertThat(context.getFieldValue(a.getId()), instanceOf(ErrorValue.class));

    }

}