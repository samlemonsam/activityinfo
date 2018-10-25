package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.hamcrest.Matchers;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;

public class SubstringFunctionTestBase {

    public void testFieldValues(SubstringFunction function, List<FieldValue> values, FieldValue expectedValue) {
        List<FieldType> types = getFieldTypes(values);
        assertThat(function.resolveResultType(types), Matchers.equalTo(types.get(0)));
        assertThat(function.apply(values), Matchers.equalTo(expectedValue));
    }

    private List<FieldType> getFieldTypes(List<FieldValue> values) {
        if (values == null) {
            return null;
        }
        return values.stream()
                .map(FieldValue::getTypeClass)
                .map(FieldTypeClass::createType)
                .collect(Collectors.toList());
    }

    public void testIncorrectArguments(SubstringFunction function, List<FieldValue> values) {
        List<FieldType> types = getFieldTypes(values);
        try {
            function.resolveResultType(types);
        } catch (ArgumentException expected) {
            // Expect to have an argument exception here as we should have incorrect arguments
        }
    }

    public void testColumnViews(SubstringFunction function, int numRows, List<ColumnView> values, ColumnView expectedValue) {
        ColumnView output = function.columnApply(numRows, values);
        assertThat(output.getType(), Matchers.equalTo(expectedValue.getType()));
        for (int i=0; i<expectedValue.numRows(); i++) {
            assertThat(output.get(i), Matchers.equalTo(expectedValue.get(i)));
        }
    }

}
