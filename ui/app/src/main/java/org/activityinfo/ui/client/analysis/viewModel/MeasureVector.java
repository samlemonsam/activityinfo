package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.HashMap;
import java.util.Map;

/**
 * The column vector of values which we are summarizing.
 */
public class MeasureVector {

    private double[] values;
    private ColumnView source;

    public MeasureVector(ColumnView value) {
        source = value;
    }

    public boolean isNumeric() {
        return source.getType() == ColumnType.NUMBER;
    }

    private static double[] toDouble(ColumnView column) {
        int numRows = column.numRows();
        double[] values = new double[column.numRows()];

        for (int i = 0; i < numRows; i++) {
            values[i] = column.getDouble(i);
        }
        return values;
    }

    /**
     * Create a double[] from a String column by assigning an integer to each
     * distinct string value.
     *
     */
    private static double[] quantitize(ColumnView column) {
        int numRows = column.numRows();
        double[] values = new double[column.numRows()];

        Map<String, Integer> stringMap = new HashMap<>();
        for (int i = 0; i < numRows; i++) {
            String value = column.getString(i);
            if(value == null) {
                values[i] = Double.NaN;
            } else {
                Integer code = stringMap.get(value);
                if(code == null) {
                    code = stringMap.size();
                    stringMap.put(value, code);
                }
                values[i] = code;
            }
        }
        return values;
    }

    /**
     *
     * Returns a double array with this vector's values.
     *
     * <p>The returned array must ***NOT*** be modified.</p>
     */
    public double[] getDoubleArray() {
        if(values == null) {
            if(source.getType() == ColumnType.STRING) {
                values = quantitize(source);
            } else {
                values = toDouble(source);
            }
        }
        return values;
    }
}
