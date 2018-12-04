package org.activityinfo.model.formula.functions;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.List;

/**
 * <p>
 * Returns a given number of characters from the start of a string. <br>
 * There are 2 input arguments:
 * <ul>
 *     <li>Argument 0: {@code text} the string to compute substring from. Can be {@link NarrativeType} or {@link TextType}.</li>
 *     <li>Argument 1: {@code numChars} the number of characters in substring. Must be {@link QuantityType} </li>
 * </ul>
 * The returned {@link FieldValue} will the same type as {@code text}
 * </p>
 */
public class LeftFunction extends SubstringFunction {

    private static final int NUM_ARGS = 2;

    public static final LeftFunction INSTANCE = new LeftFunction();

    public LeftFunction() {
        super("LEFT", NUM_ARGS);
    }

    @Override
    protected double startPosition(List<FieldValue> arguments) {
        // Start position is always 0
        return 0;
    }

    @Override
    protected double startPosition(int row, List<ColumnView> arguments) {
        // Start position is always 0
        return 0;
    }

    @Override
    protected String computeSubstring(String text, double startPosition, double numChars) {
        // For LEFT functions, we take the start position of 0 and the endIndex of 0 + numChars
        int startIndex = (int) startPosition;
        int endIndex = startIndex + (int) numChars;

        // Prevent an index out of bounds exception
        if (endIndex > text.length()) {
            return null;
        }

        return text.substring(startIndex, endIndex);
    }

}
