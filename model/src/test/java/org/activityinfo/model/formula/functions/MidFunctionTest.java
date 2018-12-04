package org.activityinfo.model.formula.functions;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.junit.Test;

public class MidFunctionTest extends SubstringFunctionTestBase {

    @Test
    public void mid() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = TextValue.valueOf("Example");
        val2 = new Quantity(2);
        val3 = new Quantity(2);
        expectedOutput = TextValue.valueOf("am");
        testFieldValues(MidFunction.INSTANCE, Lists.newArrayList(val1,val2,val3), expectedOutput);
    }

    @Test
    public void midNarrative() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(2);
        val3 = new Quantity(2);
        expectedOutput = NarrativeValue.valueOf("am");
        testFieldValues(MidFunction.INSTANCE, Lists.newArrayList(val1,val2,val3), expectedOutput);
    }

    @Test
    public void midIncorrectNumCharArgument() {
        FieldValue val1, val2, val3;
        val1 = NarrativeValue.valueOf("Example");
        val2 = TextValue.valueOf("2");
        val3 = new Quantity(2);
        testIncorrectArguments(MidFunction.INSTANCE, Lists.newArrayList(val1,val2,val3));
    }

    @Test
    public void midEmptyNumCharArgument() {
        FieldValue val1, val2, val3;
        val1 = NarrativeValue.valueOf("Example");
        val2 = NullFieldValue.INSTANCE;
        val3 = new Quantity(2);
        testIncorrectArguments(MidFunction.INSTANCE, Lists.newArrayList(val1,val2,val3));
    }

    @Test
    public void indexOutOfBoundsTest() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = NarrativeValue.valueOf("Example");
        val2 = new Quantity(5);
        val3 = new Quantity(5);
        expectedOutput = NarrativeValue.valueOf(null);
        testFieldValues(MidFunction.INSTANCE, Lists.newArrayList(val1,val2,val3), expectedOutput);
    }

    @Test
    public void midCol() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("Example1", "Example2", "Example3"));
        col2 = new ConstantColumnView(3, 3);
        col3 = new ConstantColumnView(3, 5);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", "le2", "le3"));
        testColumnViews(MidFunction.INSTANCE, 3, Lists.newArrayList(col1,col2,col3), expectedOutput);
    }

    @Test
    public void midColWithMissingStrings() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("Example1", null, "Example3"));
        col2 = new ConstantColumnView(3, 3);
        col3 = new ConstantColumnView(3, 5);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", null, "le3"));
        testColumnViews(MidFunction.INSTANCE, 3, Lists.newArrayList(col1,col2,col3), expectedOutput);
    }

    @Test
    public void midColWithMissingNumChars() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("Example1", "Example2", "Example3"));
        col2 = new DoubleArrayColumnView(new double[] {3.0, Double.NaN, 3.0});
        col3 = new ConstantColumnView(3, 5);
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", null, "le3"));
        testColumnViews(MidFunction.INSTANCE, 3, Lists.newArrayList(col1,col2,col3), expectedOutput);
    }

    @Test
    public void midColWithMissingStartPos() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("Example1", "Example2", "Example3"));
        col2 = new ConstantColumnView(3, 3);
        col3 = new DoubleArrayColumnView(new double[] {5.0, Double.NaN, 5.0});
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("le1", null, "le3"));
        testColumnViews(MidFunction.INSTANCE, 3, Lists.newArrayList(col1,col2,col3), expectedOutput);
    }

}