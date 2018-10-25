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

public class RightFunctionTest extends SubstringFunctionTestBase {

    @Test
    public void right() {
        FieldValue val1, val2, expectedOutput;
        val1 = TextValue.valueOf("Example");
        val2 = new Quantity(2);
        expectedOutput = TextValue.valueOf("le");
        testFieldValues(RightFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void rightNarrative() {
        FieldValue val1, val2, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(2);
        expectedOutput = NarrativeValue.valueOf("le");
        testFieldValues(RightFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void rightIncorrectNumCharArgument() {
        FieldValue val1, val2;
        val1 = NarrativeValue.valueOf("Example");
        val2 = TextValue.valueOf("2");
        testIncorrectArguments(RightFunction.INSTANCE, Lists.newArrayList(val1,val2));
    }

    @Test
    public void rightEmptyNumCharArgument() {
        FieldValue val1, val2;
        val1 = NarrativeValue.valueOf("Example");
        val2 = NullFieldValue.INSTANCE;
        testIncorrectArguments(RightFunction.INSTANCE, Lists.newArrayList(val1,val2));
    }

    @Test
    public void indexOutOfBoundsTest() {
        FieldValue val1, val2, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(10);
        expectedOutput = NarrativeValue.valueOf(null);
        testFieldValues(RightFunction.INSTANCE, Lists.newArrayList(val1,val2), expectedOutput);
    }

    @Test
    public void rightCol() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", "2Example2", "3Example3"));
        col2 = new ConstantColumnView(3, 3);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", "le2", "le3"));
        testColumnViews(RightFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

    @Test
    public void rightColWithMissingStrings() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", null, "3Example3"));
        col2 = new ConstantColumnView(3, 3);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", null, "le3"));
        testColumnViews(RightFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

    @Test
    public void rightColWithMissingNumChars() {
        ColumnView col1, col2, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1Example1", "2Example2", "3Example3"));
        col2 = new DoubleArrayColumnView(new double[] {3.0, Double.NaN, 3.0});
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", null, "le3"));
        testColumnViews(RightFunction.INSTANCE, 3, Lists.newArrayList(col1,col2), expectedOutput);
    }

}