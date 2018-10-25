package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.diagnostic.ArgumentException;
import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.List;

public abstract class SubstringFunction extends FormulaFunction implements ColumnFunction {

    private static final int MAX_ARGS = 3;

    protected static final int TEXT_ARG = 0;
    protected static final int NUM_CHARS_ARG = 1;
    protected static final int START_POS_ARG = 2;

    private String name;
    private int numArgs;

    public SubstringFunction(String name, int numArgs) {
        assert numArgs <= MAX_ARGS;
        this.name = name;
        this.numArgs = numArgs;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        checkArity(argumentTypes, numArgs);
        checkArguments(argumentTypes);
        return argumentTypes.get(0);
    }

    private void checkArguments(List<FieldType> argumentTypes) {
        checkStringArgument(0, argumentTypes.get(0));
        for (int i=1; i<numArgs; i++) {
            checkNumberArgument(i, argumentTypes.get(i));
        }
    }

    private void checkStringArgument(int argIndex, FieldType argumentType) {
        if (argumentType instanceof TextType || argumentType instanceof NarrativeType) {
            return;
        }
        throw new ArgumentException(argIndex, "Must be text value (TextType or NarrativeType)");
    }

    private void checkNumberArgument(int argIndex, FieldType argumentType) {
        if (argumentType instanceof QuantityType) {
            return;
        }
        throw new ArgumentException(argIndex, "Must be Quantity value");
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments, numArgs);

        String substring = substring(text(arguments), startPosition(arguments), numChars(arguments));

        if (arguments.get(0).getTypeClass().equals(FieldTypeClass.NARRATIVE)) {
            return NarrativeValue.valueOf(substring);
        } else {
            return TextValue.valueOf(substring);
        }
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments, numArgs);
        checkColumnLengths(numRows, arguments);

        String[] substring = new String[numRows];
        for (int i=0; i<numRows; i++) {
            substring[i] = substring(text(i,arguments), startPosition(i,arguments), numChars(i,arguments));
        }

        return new StringArrayColumnView(substring);
    }

    private String substring(String text, double startPosition, double numChars) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return "";
        }
        if (Double.isNaN(startPosition) || Double.isInfinite(startPosition) || startPosition > Integer.MAX_VALUE) {
            return null;
        }
        if (Double.isNaN(numChars) || Double.isInfinite(numChars) || numChars > Integer.MAX_VALUE) {
            return null;
        }
        return computeSubstring(text, startPosition, numChars);
    }

    private void checkColumnLengths(int numRows, List<ColumnView> arguments) {
        for (ColumnView arg : arguments) {
            if (arg.numRows() != numRows) {
                throw new FormulaSyntaxException("Columns must have same number of rows.");
            }
        }
    }

    private String text(List<FieldValue> arguments) {
        FieldValue text = arguments.get(TEXT_ARG);
        if (text instanceof NullFieldValue || text instanceof ErrorValue) {
            return null;
        }
        return Casting.toString(arguments.get(TEXT_ARG));
    }

    private String text(int row, List<ColumnView> arguments) {
        return arguments.get(TEXT_ARG).getString(row);
    }

    private double numChars(List<FieldValue> arguments) {
        FieldValue numChars = arguments.get(NUM_CHARS_ARG);
        if (numChars instanceof NullFieldValue || numChars instanceof ErrorValue) {
            return Double.NaN;
        }
        return Casting.toQuantity(numChars).getValue();
    }

    private double numChars(int row, List<ColumnView> arguments) {
        return arguments.get(NUM_CHARS_ARG).getDouble(row);
    }

    protected abstract double startPosition(List<FieldValue> arguments);

    protected abstract double startPosition(int row, List<ColumnView> arguments);

    protected abstract String computeSubstring(String text, double startPosition, double numChars);



}
