package org.activityinfo.model.formula.functions;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.List;

public class ConcatenateFunction extends FormulaFunction implements ColumnFunction {

    public static final ConcatenateFunction INSTANCE = new ConcatenateFunction();

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private ConcatenateFunction() {
    }

    Stopwatch getStopwatch() {
        return stopwatch;
    }

    @Override
    public String getId() {
        return "CONCAT";
    }

    @Override
    public String getLabel() {
        return "CONCAT";
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkRowLength(numRows, arguments);

        String[] concatenation = new String[numRows];
        StringBuilder builder = new StringBuilder();

        for (int i=0; i<numRows; i++) {
            concatenation[i] = concatenateRow(builder, i, arguments);
        }

        return new StringArrayColumnView(concatenation);
    }

    private String concatenateRow(StringBuilder builder, int row, List<ColumnView> arguments) {
        builder.delete(0, builder.length());
        for (ColumnView column : arguments) {
            builder.append(Strings.nullToEmpty(column.getString(row)));
        }
        return builder.toString();
    }

    private void checkRowLength(int numRows, List<ColumnView> arguments) {
        for (ColumnView argument : arguments) {
            if (argument.numRows() != numRows) {
                throw new FormulaSyntaxException("Columns must have same number of rows.");
            }
        }
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        StringBuilder concatenatedString = new StringBuilder();
        for (int i=0; i<arguments.size(); i++) {
            if (arguments.get(i) == null) {
                continue;
            }
            String value = Casting.toString(arguments.get(i));
            concatenatedString.append(value);
        }
        return TextValue.valueOf(concatenatedString.toString());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        for (int i=0; i<argumentTypes.size(); i++) {
            if (argumentTypes.get(i) instanceof TextType) {
                continue;
            }
            if (argumentTypes.get(i) instanceof NarrativeType) {
                continue;
            }
            throw new ArgumentException(i, "Expected text value (TextType or NarrativeType)");
        }
        return NarrativeType.INSTANCE;
    }
}