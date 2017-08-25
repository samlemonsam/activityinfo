package org.activityinfo.server.endpoint.odk;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.io.xform.form.Bind;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.io.xform.util.XFormNavigator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TFormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.endpoint.odk.build.XFormBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author yuriyz on 10/27/2015.
 */
@SuppressWarnings("AppEngineForbiddenCode")
@RunWith(InjectionSupport.class)
public class XPathBuilderTest extends CommandTestCase2 {

    private static final ResourceId GENDER_FIELD_ID = ResourceId.generateId();
    private static final ResourceId PREGNANT_FIELD_ID = ResourceId.generateId();
    private static final ResourceId TEXT_FIELD_ID = ResourceId.valueOf("test_text");
    private static final ResourceId QUANTITY_FIELD_ID = ResourceId.valueOf("test_quantity");

    private OdkFormFieldBuilderFactory factory;

    @Before
    public void setUp() throws IOException {
        factory = new OdkFormFieldBuilderFactory(injector.getInstance(ResourceLocatorSync.class));
    }

    @Test
    public void equals() {

        TFormClass formClass = createFormClass();

        ResourceId male = formClass.getEnumValueByLabel("Male").getId();

        formClass.getFormClass().getField(GENDER_FIELD_ID).setRelevanceConditionExpression(
                        String.format("{%s}==\"%s\"", GENDER_FIELD_ID.asString(), male.asString()));

        XForm xForm = xForm(formClass.getFormClass());

        assertEquals(
                bindByFieldId(GENDER_FIELD_ID, xForm).getRelevant(),
                String.format("/data/field_%s = \"%s\"", GENDER_FIELD_ID.asString(), male.asString()));
    }

    @Test
    public void containsAny() {
        TFormClass formClass = createFormClass();

        ResourceId male = formClass.getEnumValueByLabel("Male").getId();
        ResourceId female = formClass.getEnumValueByLabel("Female").getId();

        formClass.getFormClass().getField(GENDER_FIELD_ID).setRelevanceConditionExpression(
                String.format("containsAny({%s},{%s})", GENDER_FIELD_ID.asString(), male.asString()));

        XForm xForm = xForm(formClass.getFormClass());

        assertEquals(
                bindByFieldId(GENDER_FIELD_ID, xForm).getRelevant(),
                String.format("selected(/data/field_%s, '%s')", GENDER_FIELD_ID.asString(), male.asString()));

        formClass.getFormClass().getField(GENDER_FIELD_ID).setRelevanceConditionExpression(
                String.format("containsAny({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), male.asString(), female.asString()));

        xForm = xForm(formClass.getFormClass());

        assertEquals(
                bindByFieldId(GENDER_FIELD_ID, xForm).getRelevant(),
                String.format("selected(/data/field_%s, '%s') or selected(/data/field_%s, '%s')",
                        GENDER_FIELD_ID.asString(), male.asString(), GENDER_FIELD_ID.asString(), female.asString()));
    }

    @Test
    public void containsAll() {
        TFormClass formClass = createFormClass();

        ResourceId male = formClass.getEnumValueByLabel("Male").getId();
        ResourceId female = formClass.getEnumValueByLabel("Female").getId();

        formClass.getFormClass().getField(GENDER_FIELD_ID).setRelevanceConditionExpression(
                String.format("containsAll({%s},{%s})", GENDER_FIELD_ID.asString(), male.asString()));

        XForm xForm = xForm(formClass.getFormClass());

        assertEquals(
                bindByFieldId(GENDER_FIELD_ID, xForm).getRelevant(),
                String.format("selected(/data/field_%s, '%s')", GENDER_FIELD_ID.asString(), male.asString()));

        formClass.getFormClass().getField(GENDER_FIELD_ID).setRelevanceConditionExpression(
                String.format("containsAll({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), male.asString(), female.asString()));

        xForm = xForm(formClass.getFormClass());

        assertEquals(
                bindByFieldId(GENDER_FIELD_ID, xForm).getRelevant(),
                String.format("selected(/data/field_%s, '%s') and selected(/data/field_%s, '%s')",
                        GENDER_FIELD_ID.asString(), male.asString(), GENDER_FIELD_ID.asString(), female.asString()));
    }

    private Bind bindByFieldId(ResourceId fieldId, XForm xForm) {
        return XFormNavigator.getBind("/data/field_" + fieldId.asString(), xForm);
    }

    private XForm xForm(FormClass formClass) {
        return new XFormBuilder(factory)
                .setUserId("yuriyz")
                .build(formClass);
    }

    private static TFormClass createFormClass() {
        EnumItem male = new EnumItem(ResourceId.generateId(), "Male");
        EnumItem female = new EnumItem(ResourceId.generateId(), "Female");

        EnumItem pregnantYes = new EnumItem(ResourceId.generateId(), "Yes");
        EnumItem pregnantNo = new EnumItem(ResourceId.generateId(), "No");

        FormField genderField = new FormField(GENDER_FIELD_ID);
        genderField.setLabel("Gender");
        genderField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(male, female)));

        FormField pregnantField = new FormField(PREGNANT_FIELD_ID);
        pregnantField.setLabel("are you currently pregnant?");
        pregnantField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(pregnantYes, pregnantNo)));

        FormField textField = new FormField(TEXT_FIELD_ID);
        textField.setLabel("Text");
        textField.setType(TextType.SIMPLE);

        FormField quantityField = new FormField(QUANTITY_FIELD_ID);
        quantityField.setLabel("Quantity");
        quantityField.setType(QuantityType.TYPE_CLASS.createType());

        final FormClass formClass = new FormClass(CuidAdapter.activityFormClass(1));
        formClass.addElement(genderField);
        formClass.addElement(pregnantField);
        formClass.addElement(textField);
        formClass.addElement(quantityField);
        return new TFormClass(formClass);
    }
}
