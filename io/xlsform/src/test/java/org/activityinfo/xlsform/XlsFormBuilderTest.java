package org.activityinfo.xlsform;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateType;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

public class XlsFormBuilderTest {

    @Test
    public void test() throws IOException {

        
        final FormClass formClass = new FormClass(ResourceId.valueOf("F1"));
        
        formClass.addField(ResourceId.valueOf("X1"))
                .setLabel("What is your name?")
                .setRequired(true)
                .setCode("RespName")
                .setType(TextType.SIMPLE);
        
        formClass.addField(ResourceId.valueOf("X2"))
                .setLabel("How old are you?")
                .setRequired(true)
                .setCode("Age")
                .setType(new QuantityType("years"));
        
        formClass.addField(ResourceId.valueOf("X3"))
                .setLabel("Age in dog years")
                .setRequired(true)
                .setCode("AgeDogYears")
                .setType(new CalculatedFieldType("Age*7"));
        
        formClass.addField(ResourceId.valueOf("X4"))
                .setLabel("Date of birth")
                .setRequired(false)
                .setCode("DOB")
                .setType(LocalDateType.INSTANCE);
        
        formClass.addField(ResourceId.valueOf("X5"))
                .setLabel("What is your favorite color?")
                .setRequired(false)
                .setCode("Color")
                .setType(new EnumType(Cardinality.SINGLE,
                        Lists.newArrayList(
                                new EnumItem(ResourceId.valueOf("Z1"), "Blue"),
                                new EnumItem(ResourceId.valueOf("Z2"), "Red"),
                                new EnumItem(ResourceId.valueOf("Z3"), "Green"))));
        
        formClass.addField(ResourceId.valueOf("X6"))
                .setLabel("Who are your brothers?")
                .setCode("Brothers")
                .setType(new SubFormReferenceType(ResourceId.valueOf("F2")));
        
        
        final FormClass subFormClass = new FormClass(ResourceId.valueOf("F2"));
        subFormClass.setLabel("Sub Form");
        subFormClass.addField(ResourceId.valueOf("Y1"))
            .setLabel("What is his name?")
            .setType(TextType.SIMPLE)
            .setCode("Name");


        FormClassProvider provider = new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId resourceId) {
                if (resourceId.equals(formClass.getId())) {
                    return formClass;
                } else if (resourceId.equals(subFormClass.getId())) {
                    return subFormClass;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        
        
        XlsFormBuilder writer = new XlsFormBuilder(provider);
        writer.build(formClass.getId());
        
        try(FileOutputStream fos = new FileOutputStream("test.xls")) {
            writer.write(fos);
        }
        
    }
}