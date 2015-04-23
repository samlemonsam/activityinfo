package org.activityinfo.ui.client.component.formdesigner.skip;
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

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.activityinfo.model.expr.functions.BooleanFunctions;
import org.activityinfo.model.expr.functions.ContainsAllFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author yuriyz on 7/28/14.
 */
public class ExpressionBuilderTest {

    private static final ResourceId GENDER_FIELD_ID = ResourceId.valueOf("test_f1");
    private static final ResourceId PREGNANT_FIELD_ID = ResourceId.valueOf("test_f2");
    private static final ResourceId TEXT_FIELD_ID = ResourceId.valueOf("test_text");

    private static final EnumItem MALE = new EnumItem(ResourceId.valueOf("test_ev1"), "Male");
    private static final EnumItem FEMALE = new EnumItem(ResourceId.valueOf("test_ev2"), "Female");
    private static final EnumItem PREGNANT_YES = new EnumItem(ResourceId.valueOf("test_ev3"), "Yes");
    private static final EnumItem PREGNANT_NO = new EnumItem(ResourceId.valueOf("test_ev4"), "No");

    FormClass formClass;

    @Before
    public void setUp() {
        formClass = createFormClass();
    }

    @Test
    public void simpleEnumValueExpression() {
        RowData row = new RowData();
        row.setFormField(formClass.getField(GENDER_FIELD_ID));
        row.setFunction(BooleanFunctions.EQUAL);
        row.setValue(new EnumValue(Sets.newHashSet(enumValue(GENDER_FIELD_ID, "Male").getId())));
        row.setJoinFunction(BooleanFunctions.AND);

        RowData row2 = new RowData();
        row2.setFormField(formClass.getField(PREGNANT_FIELD_ID));
        row2.setFunction(BooleanFunctions.NOT_EQUAL);
        row2.setValue(new EnumValue(Sets.newHashSet(enumValue(PREGNANT_FIELD_ID, "No").getId())));
        row2.setJoinFunction(BooleanFunctions.OR);

        assertCorrectRoundTripTranslation("{test_f1}=={test_ev1}", row);
        assertCorrectRoundTripTranslation("({test_f1}=={test_ev1})||({test_f2}!={test_ev4})", row, row2);

        row2.setJoinFunction(BooleanFunctions.AND);
        assertCorrectRoundTripTranslation("({test_f1}=={test_ev1})&&({test_f2}!={test_ev4})", row, row2);

        row2.setValue(new EnumValue(Sets.newHashSet(
                enumValue(PREGNANT_FIELD_ID, "Yes").getId(),
                enumValue(PREGNANT_FIELD_ID, "No").getId())));
        assertCorrectRoundTripTranslation("(({test_f2}!={test_ev3})&&({test_f2}!={test_ev4}))", row2);
        assertCorrectRoundTripTranslation("({test_f1}=={test_ev1})&&(({test_f2}!={test_ev3})&&({test_f2}!={test_ev4}))", row, row2);

        // containsAll/containsAny
        row.setFunction(ContainsAllFunction.INSTANCE);
        assertCorrectRoundTripTranslation("containsAll({test_f1},{test_ev1})", row);
    }

    @Test
    public void text() {
        RowData row = new RowData();
        row.setFormField(formClass.getField(TEXT_FIELD_ID));
        row.setFunction(BooleanFunctions.EQUAL);
        row.setValue(TextValue.valueOf("val"));
        row.setJoinFunction(BooleanFunctions.AND);

        assertCorrectRoundTripTranslation("{test_text}==\"val\"", row);
    }

    private void assertCorrectRoundTripTranslation(String expectedExpression, RowData... rows) {
        // Verify that we can go from rows -> expression
        List<RowData> rowList = Arrays.asList(rows);
        String builtExpression = new ExpressionBuilder(rowList).build();
        assertThat("For conditions: " + Joiner.on(", ").join(rowList),
                builtExpression, equalTo(expectedExpression));

        // And that we can go from expression -> rows
        RowDataBuilder builder = new RowDataBuilder(formClass);
        List<RowData> createRows = builder.build(builtExpression);
        assertEquals(builtExpression, rowList, createRows);
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


    private static FormClass createFormClass() {
        FormField genderField = new FormField(GENDER_FIELD_ID);
        genderField.setLabel("Gender");
        genderField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(MALE, FEMALE)));

        FormField pregnantField = new FormField(PREGNANT_FIELD_ID);
        pregnantField.setLabel("are you currently pregnant?");
        pregnantField.setType(new EnumType(Cardinality.SINGLE, Arrays.asList(PREGNANT_YES, PREGNANT_NO)));

        FormField textField = new FormField(TEXT_FIELD_ID);
        textField.setLabel("Text");
        textField.setType(TextType.INSTANCE);

        final FormClass formClass = new FormClass(CuidAdapter.activityFormClass(1));
        formClass.addElement(genderField);
        formClass.addElement(pregnantField);
        formClass.addElement(textField);
        return formClass;
    }
}
