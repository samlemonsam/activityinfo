package org.activityinfo.model.expr;
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

import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.expr.functions.Casting;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author yuriyz on 7/24/14.
 */

public class SkipExpressionTest {

    private static final ResourceId GENDER_FIELD_ID = ResourceId.generateId();
    private static final ResourceId PREGNANT_FIELD_ID = ResourceId.generateId();
    private static final ResourceId TEXT_FIELD_ID = ResourceId.valueOf("test_text");
    private static final ResourceId QUANTITY_FIELD_ID = ResourceId.valueOf("test_quantity");

    FormClass formClass;

    @Before
    public void setUp() {
        formClass = createFormClass();
    }

    @Test
    public void enumType() {
        FormInstance instance = new FormInstance(ResourceId.generateId(), formClass.getId());
        instance.set(GENDER_FIELD_ID, enumFieldValue(GENDER_FIELD_ID, "Male"));

        eval(String.format("{%s}=={%s}", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), true, instance);
        eval(String.format("{%s}!={%s}", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), false, instance);
        eval(String.format("{%s}=={%s}", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Female").getId()), false, instance);
        eval(String.format("({%s}=={%s})&&({%s}!={%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Female").getId()), true, instance);

        // contains all
        eval(String.format("containsAll({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), true, instance);
        eval(String.format("containsAll({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Female").getId()), false, instance);
        eval(String.format("containsAll({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()), true, instance);

        eval(String.format("containsAny({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), true, instance);
        eval(String.format("containsAny({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()), true, instance);
        eval(String.format("containsAny({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Female").getId()), false, instance);

        // enum with 2 values
        instance.set(GENDER_FIELD_ID,  new EnumValue(enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()));

        eval(String.format("containsAny({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), true, instance);

        eval(String.format("containsAll({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()), true, instance);

        eval(String.format("notContainsAll({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), true, instance);
        eval(String.format("notContainsAll({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()), false, instance);

        eval(String.format("notContainsAny({%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId()), false, instance);
        eval(String.format("notContainsAny({%s},{%s},{%s})", GENDER_FIELD_ID.asString(), enumValue(GENDER_FIELD_ID, "Male").getId(), enumValue(GENDER_FIELD_ID, "Female").getId()), false, instance);

    }

    @Test
    public void text() {
        FormInstance instance = new FormInstance(ResourceId.generateId(), formClass.getId());
        instance.set(TEXT_FIELD_ID, "1");

        eval(String.format("{%s}==\"1\"", TEXT_FIELD_ID.asString()), true, instance);
        eval(String.format("{%s}!=\"1\"", TEXT_FIELD_ID.asString()), false, instance);
    }

    @Test
    public void quantity() {
        FormInstance instance = new FormInstance(ResourceId.generateId(), formClass.getId());
        instance.set(QUANTITY_FIELD_ID, 3);

        eval(String.format("{%s}==3", QUANTITY_FIELD_ID.asString()), true, instance);
        eval(String.format("{%s}==3.0", QUANTITY_FIELD_ID.asString()), true, instance);
        eval(String.format("{%s}!=3", QUANTITY_FIELD_ID.asString()), false, instance);
    }


    private void eval(String skipExpression, boolean expectedValue, FormInstance instance) {
        ExprLexer lexer = new ExprLexer(skipExpression);
        ExprParser parser = new ExprParser(lexer);
        ExprNode expr = parser.parse();
        Assert.assertEquals(skipExpression, expectedValue,
                Casting.toBoolean(expr.evaluate(new FormEvalContext(formClass, instance))));
    }

    private EnumItem enumValue(ResourceId formField, String label) {
        EnumType enumType = (EnumType) formClass.getField(formField).getType();
        for (EnumItem value : enumType.getValues()) {
            if (value.getLabel().equalsIgnoreCase(label)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unable to find enumValue with label: " + label);
    }

    private EnumValue enumFieldValue(ResourceId formField, String label) {
        return new EnumValue(enumValue(formField, label).getId());
    }


    private static FormClass createFormClass() {
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
        textField.setType(TextType.INSTANCE);

        FormField quantityField = new FormField(QUANTITY_FIELD_ID);
        quantityField.setLabel("Quantity");
        quantityField.setType(QuantityType.TYPE_CLASS.createType());

        final FormClass formClass = new FormClass(CuidAdapter.activityFormClass(1));
        formClass.addElement(genderField);
        formClass.addElement(pregnantField);
        formClass.addElement(textField);
        formClass.addElement(quantityField);
        return formClass;
    }
}
