package org.activityinfo.model.formula.functions;

import com.google.common.collect.Lists;
import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.query.*;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;

public class ConcatenateFunctionTest {

    @Test
    public void concatenateTextFields() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = TextValue.valueOf("Left");
        val2 = TextValue.valueOf("Middle");
        val3 = TextValue.valueOf("Right");
        expectedOutput = TextValue.valueOf("LeftMiddleRight");

        List<FieldValue> values = Lists.newArrayList(val1, val2, val3);
        List<FieldType> types = values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());

        assertThat(resolveFieldType(types), Matchers.equalTo(NarrativeType.INSTANCE));
        assertThat(ConcatenateFunction.INSTANCE.apply(values), Matchers.equalTo(expectedOutput));
    }

    @Test
    public void concatenateNarrativeFields() {
        FieldValue val1, val2, val3,expectedOutput;
        val1 = NarrativeValue.valueOf("Left");
        val2 = NarrativeValue.valueOf("Middle");
        val3 = NarrativeValue.valueOf("Right");
        expectedOutput = TextValue.valueOf("LeftMiddleRight");

        List<FieldValue> values = Lists.newArrayList(val1, val2, val3);
        List<FieldType> types = values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());

        assertThat(resolveFieldType(types), Matchers.equalTo(NarrativeType.INSTANCE));
        assertThat(ConcatenateFunction.INSTANCE.apply(values), Matchers.equalTo(expectedOutput));
    }

    @Test
    public void concatenateMixedTextFields() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = NarrativeValue.valueOf("Left");
        val2 = TextValue.valueOf("Middle");
        val3 = NarrativeValue.valueOf("Right");
        expectedOutput = TextValue.valueOf("LeftMiddleRight");

        List<FieldValue> values = Lists.newArrayList(val1, val2, val3);
        List<FieldType> types = values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());

        assertThat(resolveFieldType(types), Matchers.equalTo(NarrativeType.INSTANCE));
        assertThat(ConcatenateFunction.INSTANCE.apply(values), Matchers.equalTo(expectedOutput));
    }

    public FieldType resolveFieldType(List<FieldType> fieldTypes) {
        return ConcatenateFunction.INSTANCE.resolveResultType(fieldTypes);
    }

    @Test
    public void tryResolveNonTextFields() {
        FieldValue val1, val2, val3;
        val1 = NarrativeValue.valueOf("Left");
        val2 = new Quantity(2);
        val3 = BarcodeValue.valueOf("1234");

        List<FieldValue> values = Lists.newArrayList(val1, val2, val3);
        List<FieldType> types = values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());

        try {
            resolveFieldType(types);
        } catch(ArgumentException expected) {
            // Expect to have an argument exception here as Quantity/Barcode are not supported
        }
    }

    @Test
    public void concatenateSingleField() {
        FieldValue val1, expectedOutput;
        val1 = TextValue.valueOf("Left");
        expectedOutput = TextValue.valueOf("Left");

        List<FieldValue> values = Lists.newArrayList(val1);
        List<FieldType> types = values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());

        assertThat(resolveFieldType(types), Matchers.equalTo(NarrativeType.INSTANCE));
        assertThat(ConcatenateFunction.INSTANCE.apply(values), Matchers.equalTo(expectedOutput));
    }

    @Test
    public void concatenateNullFields() {
        FieldValue val1, val2, val3, expectedOutput;
        val1 = TextValue.valueOf("Left");
        val2 = TextValue.valueOf(null);
        val3 = TextValue.valueOf("Right");
        expectedOutput = TextValue.valueOf("LeftRight");

        List<FieldValue> values = Lists.newArrayList(val1, val2, val3);
        assertThat(ConcatenateFunction.INSTANCE.apply(values), Matchers.equalTo(expectedOutput));
    }

    @Test
    public void concatenateStringColumns() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1", "2", "3"));
        col2 = new ConstantColumnView(3, "const");
        col3 = new StringArrayColumnView(Lists.newArrayList("One", "Two", "Three"));
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("1constOne", "2constTwo", "3constThree"));

        List<ColumnView> columns = Lists.newArrayList(col1, col2, col3);
        ColumnView output = ConcatenateFunction.INSTANCE.columnApply(3, columns);

        assertThat(output.getType(), Matchers.equalTo(expectedOutput.getType()));
        assertThat(output.get(0), Matchers.equalTo(expectedOutput.get(0)));
        assertThat(output.get(1), Matchers.equalTo(expectedOutput.get(1)));
        assertThat(output.get(2), Matchers.equalTo(expectedOutput.get(2)));
    }

    @Test
    public void concatenateStringAndEmptyColumns() {
        ColumnView col1, col2, col3, expectedOutput;
        col1 = new StringArrayColumnView(Lists.newArrayList("1", "2", "3"));
        col2 = new EmptyColumnView(ColumnType.STRING, 3);
        col3 = new StringArrayColumnView(Lists.newArrayList("One", "Two", "Three"));
        expectedOutput = new StringArrayColumnView(Lists.newArrayList("1One", "2Two", "3Three"));

        List<ColumnView> columns = Lists.newArrayList(col1, col2, col3);
        ColumnView output = ConcatenateFunction.INSTANCE.columnApply(3, columns);

        assertThat(output.getType(), Matchers.equalTo(expectedOutput.getType()));
        assertThat(output.get(0), Matchers.equalTo(expectedOutput.get(0)));
        assertThat(output.get(1), Matchers.equalTo(expectedOutput.get(1)));
        assertThat(output.get(2), Matchers.equalTo(expectedOutput.get(2)));
    }

}