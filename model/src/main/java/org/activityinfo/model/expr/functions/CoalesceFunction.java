package org.activityinfo.model.expr.functions;

import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.model.query.*;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

/**
 * Takes the first non-null value of its arguments
 */
public class CoalesceFunction extends ExprFunction implements ColumnFunction {

    public static final CoalesceFunction INSTANCE = new CoalesceFunction();

    private CoalesceFunction() {
    }

    @Override
    public String getId() {
        return "coalesce";
    }

    @Override
    public String getLabel() {
        return "coalesce";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        for (FieldValue argument : arguments) {
            if(argument != null && argument != NullFieldValue.INSTANCE) {
                return argument;
            }
        }
        return NullFieldValue.INSTANCE;
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        if(argumentTypes.isEmpty()) {
            return new QuantityType();
        }
        return argumentTypes.get(0);
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        if(arguments.isEmpty()) {
            return new EmptyColumnView(ColumnType.NUMBER, numRows);
        }
        ColumnView[] cols = arguments.toArray(new ColumnView[arguments.size()]);
        ColumnType columnType = cols[0].getType();

        switch(columnType) {
            case STRING:
                return combineString(cols);
            case NUMBER:
                return combineDouble(cols);
            case BOOLEAN:
                return combineBoolean(cols);
        }
        throw new UnsupportedOperationException();
    }


    private ColumnView combineString(ColumnView[] cols) {
        int numRows = cols[0].numRows();
        int numCols = cols.length;

        String[] values = new String[numRows];

        for(int i=0;i!=numRows;++i) {
            for(int j=0;j!=numCols;++j) {
                String value = cols[j].getString(i);
                if(value != null) {
                    values[i] = value;
                    break;
                }
            }
        }

        return new StringArrayColumnView(values);
    }

    @VisibleForTesting
    static ColumnView combineDouble(ColumnView[] cols) {
        int numRows = cols[0].numRows();
        int numCols = cols.length;

        double[] values = new double[numRows];

        for(int i=0;i!=numRows;++i) {
            values[i] = Double.NaN;
            for(int j=0;j!=numCols;++j) {
                double value = cols[j].getDouble(i);
                if(!Double.isNaN(value)) {
                    values[i] = value;
                    break;
                }
            }
        }
        return new DoubleArrayColumnView(values);
    }


    private ColumnView combineBoolean(ColumnView[] cols) {
        int numRows = cols[0].numRows();
        int numCols = cols.length;

        int[] values = new int[numRows];


        for(int i=0;i!=numRows;++i) {
            values[i] = ColumnView.NA;
            for(int j=0;j!=numCols;++j) {
                int value = cols[j].getBoolean(j);
                if(value != ColumnView.NA) {
                    values[i] = value;
                    break;
                }
            }
        }
        return new BooleanColumnView(values);
    }
}
