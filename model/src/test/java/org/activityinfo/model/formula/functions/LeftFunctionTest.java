package org.activityinfo.model.formula.functions;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.junit.Test;

public class LeftFunctionTest extends SubstringFunctionTestBase {

    @Test
    public void left() {
        FieldValue val1, val2, expectedOutput;
        val1 = TextValue.valueOf("Example");
        val2 = new Quantity(2);
        expectedOutput = TextValue.valueOf("Ex");
        testFieldValues(LeftFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void leftNarrative() {
        FieldValue val1, val2, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(2);
        expectedOutput = NarrativeValue.valueOf("Ex");
        testFieldValues(LeftFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void leftIncorrectNumCharArgument() {
        FieldValue val1, val2;
        val1 = NarrativeValue.valueOf("Example");
        val2 = TextValue.valueOf("2");
        testIncorrectArguments(LeftFunction.INSTANCE, Lists.newArrayList(val1,val2));
    }

    @Test
    public void leftEmptyNumCharArgument() {
        FieldValue val1, val2;
        val1 = NarrativeValue.valueOf("Example");
        val2 = NullFieldValue.INSTANCE;
        testIncorrectArguments(LeftFunction.INSTANCE, Lists.newArrayList(val1,val2));
    }

    @Test
    public void indexOutOfBoundsTest() {
        FieldValue val1, val2, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(10);
        expectedOutput = NarrativeValue.valueOf(null);
        testFieldValues(LeftFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void leftCol() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", "2Example2", "3Example3"));
        col2 = new ConstantColumnView(3, 3);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("1Ex", "2Ex", "3Ex"));
        testColumnViews(LeftFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

    @Test
    public void midColWithMissingStrings() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", null, "3Example3"));
        col2 = new ConstantColumnView(3, 3);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("1Ex", null, "3Ex"));
        testColumnViews(LeftFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

    @Test
    public void midColWithMissingNumChars() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", "2Example2", "3Example3"));
        col2 = new DoubleArrayColumnView(new double[] {3.0, Double.NaN, 3.0});
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("1Ex", null, "3Ex"));
        testColumnViews(LeftFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

}