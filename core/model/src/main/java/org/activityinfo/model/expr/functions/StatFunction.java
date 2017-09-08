package org.activityinfo.model.expr.functions;

import com.google.common.collect.Sets;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;
import java.util.Set;

/**
 * Base class for functions which return a statistical summary of values
 */
public abstract class StatFunction extends ExprFunction implements ColumnFunction {


    @Override
    public final FieldType resolveResultType(List<FieldType> argumentTypes) {
        return new QuantityType(computeUnits(argumentTypes));
    }
    
    protected final String computeUnits(List<FieldType> argumentTypes) {
        Set<String> units = Sets.newHashSet();
        for (FieldType argumentType : argumentTypes) {
            if(argumentType instanceof QuantityType) {
                QuantityType quantityType = (QuantityType) argumentType;
                units.add(quantityType.getUnits());
            }
        }
        if(units.size() == 1) {
            return units.iterator().next();
        } else {
            return QuantityType.UNKNOWN_UNITS;
        }
    }



    /**
     * Computes the statistic over the range [{@code start}, {@code end}) 
     * of values in the given array.
     *
     * <p>The implementation may choose to sort the section of the array [start, end) in place. </p>
     */
    public abstract double compute(double[] values, int start, int end);


    @Override
    public final FieldValue apply(List<FieldValue> arguments) {
        double argumentValues[] = new double[arguments.size()];

        for (int i = 0; i < arguments.size(); i++) {
            FieldValue argument = arguments.get(i);
            if (argument instanceof Quantity) {
                Quantity quantity = (Quantity) argument;
                argumentValues[i] = quantity.getValue();
            } else {
                argumentValues[i] = Double.NaN;
            }
        }

        double result = compute(argumentValues, 0, argumentValues.length);
        return new Quantity(result);
    }



    @Override
    public final ColumnView columnApply(int numRows, List<ColumnView> arguments) {

        // Apply the statistic to each row in the table,
        // over the columns

        int numCols = arguments.size();

        double[] result = new double[numRows];

        double[] argumentValues = new double[arguments.size()];

        for(int i=0;i<numRows;++i) {

            // Collect the value from each column in the row
            for(int j=0;j<numCols;++j) {
                argumentValues[j] = arguments.get(j).getDouble(i);
            }

            // Compute the statistic for the row
            result[i] = compute(argumentValues, 0, numCols);
        }

        return new DoubleArrayColumnView(result);
    }

}
