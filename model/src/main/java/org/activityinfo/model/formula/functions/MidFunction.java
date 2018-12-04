package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

/**
 * <p>
 * Returns a given number of characters from a string, from the given starting position. <br>
 * There are 3 input arguments:
 * <ul>
 *     <li>Argument 0: {@code text} the string to compute substring from. Can be {@link NarrativeType} or {@link TextType}.</li>
 *     <li>Argument 1: {@code numChars} the number of characters in substring. Must be {@link QuantityType} </li>
 *     <li>Argument 2: {@code startPosition} the starting position in the string to extract from. Must be {@link QuantityType}</li>
 * </ul>
 * The returned {@link FieldValue} will the same type as {@code text}
 * </p>
 */
public class MidFunction extends SubstringFunction {

    private static final int NUM_ARGS = 3;

    public static final MidFunction INSTANCE = new MidFunction();

    public MidFunction() {
        super("MID", NUM_ARGS);
    }

    @Override
    protected double startPosition(List<FieldValue> arguments) {
        FieldValue startPosition = arguments.get(START_POS_ARG);
        if (startPosition instanceof NullFieldValue || startPosition instanceof ErrorValue) {
            return Double.NaN;
        }
        return Casting.toQuantity(startPosition).getValue();
    }

    @Override
    protected double startPosition(int row, List<ColumnView> arguments) {
        return arguments.get(START_POS_ARG).getDouble(row);
    }

    @Override
    protected String computeSubstring(String text, double startPosition, double numChars) {
        int startIndex = (int) startPosition;
        int endIndex = startIndex + (int) numChars;

        // Prevent an index out of bounds exception
        if (startIndex < 0 || endIndex > text.length()) {
            return null;
        }

        return text.substring(startIndex, endIndex);
    }

}
