package org.activityinfo.server.endpoint.rest;

import com.google.appengine.repackaged.com.google.common.io.Resources;
import com.google.common.base.Charsets;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.TestBatchFormClassProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class SchemaCsvWriterV3Test {


    @Test
    public void test() throws IOException {

        FormClass formClass = new FormClass(ResourceId.valueOf("FORM1"));
        formClass.addElement(new FormField(ResourceId.valueOf("F1"))
            .setCode("NAME")
            .setLabel("What is your name?")
            .setDescription("The head of household's name")
            .setRequired(true)
            .setType(TextType.INSTANCE));

        formClass.addElement(new FormField(ResourceId.valueOf("F2"))
            .setCode("AGE")
            .setLabel("What is your age?")
            .setType(new QuantityType("years"))
            .setRequired(true));

        formClass.addElement(new FormField(ResourceId.valueOf("F3"))
            .setCode("GENDER")
            .setLabel("Gender of head of household")
            .setType(new EnumType(Cardinality.SINGLE,
                    new EnumItem(ResourceId.valueOf("GF"), "Female"),
                    new EnumItem(ResourceId.valueOf("GM"), "Male")))
            .setRequired(true));

        formClass.addElement(new FormField(ResourceId.valueOf("F4"))
            .setRelevanceConditionExpression("AGE > 18 && GENDER == 'Female'")
            .setType(new EnumType(Cardinality.SINGLE,
                    new EnumItem(ResourceId.valueOf("PY"), "Yes"),
                    new EnumItem(ResourceId.valueOf("PN"), "No")))
            .setRequired(true));

        formClass.addElement(new FormField(ResourceId.valueOf("F5"))
            .setLabel("Remarks")
            .setType(NarrativeType.INSTANCE)
            .setRequired(false));

        FormClass subFormClass = new FormClass(ResourceId.valueOf("FORM2"));
        subFormClass.setSubFormKind(SubFormKind.REPEATING);

        subFormClass.addElement(new FormField(ResourceId.valueOf("F21"))
            .setLabel("Name")
            .setType(TextType.INSTANCE)
            .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F22"))
            .setLabel("Age")
            .setType(new QuantityType("years"))
            .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F23"))
            .setLabel("Gender")
            .setType(new EnumType(Cardinality.SINGLE,
                    new EnumItem(ResourceId.valueOf("GF"), "Female"),
                    new EnumItem(ResourceId.valueOf("GM"), "Male")))
            .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F24"))
            .setLabel("Vaccinations")
            .setType(new EnumType(Cardinality.MULTIPLE,
                    new EnumItem(ResourceId.valueOf("V1"), "Measles"),
                    new EnumItem(ResourceId.valueOf("V2"), "Mumps"),
                    new EnumItem(ResourceId.valueOf("V3"), "Rubella"))));


        formClass.addElement(new FormField(ResourceId.valueOf("SF"))
            .setLabel("Household members")
            .setType(new SubFormReferenceType(subFormClass.getId())));

        TestBatchFormClassProvider formClassProvider = new TestBatchFormClassProvider();
        formClassProvider.add(formClass);
        formClassProvider.add(subFormClass);

        UserDatabaseDTO db = new UserDatabaseDTO(1, "Survey DB");

        SchemaCsvWriterV3 writer = new SchemaCsvWriterV3(formClassProvider);

        writer.writeForms(db, Collections.singletonList(formClass.getId()));

        String actual = writer.toString();
        String expected = Resources.toString(
                Resources.getResource(SchemaCsvWriterV3.class, "survey.csv"),
                Charsets.UTF_8);

        assertThat(actual, equalTo(expected));
    }


}