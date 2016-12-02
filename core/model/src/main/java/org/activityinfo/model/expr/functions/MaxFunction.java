package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.util.List;

/**
 * Computes the maximum value of its arguments
 */
public class MaxFunction extends StatFunction implements ColumnFunction {
    
    public static final MaxFunction INSTANCE = new MaxFunction();
    
    private MaxFunction() {
    }
    
    @Override
    public String getId() {
        return "max";
    }

    @Override
    public String getLabel() {
        return "max";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        double maxValue = Double.NaN;
        String maxUnits = Quantity.UNKNOWN_UNITS;

        for (FieldValue argument : arguments) {
            if(argument instanceof Quantity) {
                Quantity quantityArgument = (Quantity) argument;
                double value = quantityArgument.getValue();
                if(!Double.isNaN(value)) {
                    if(Double.isNaN(maxValue) || value > maxValue) {
                        maxValue = value;
                        maxUnits = quantityArgument.getUnits();
                    }
                }
            }
        }
    
        return new Quantity(maxValue, maxUnits);
    }

    @Override
    public ColumnView columnApply(List<ColumnView> arguments) {
        int numRows = arguments.get(0).numRows();
        double[] result = new double[numRows];

        for(int i=0;i<numRows;++i) {
            double max = Double.NaN;
            for(int j=0;j<arguments.size();++j) {
                double value = arguments.get(j).getDouble(i);
                if(Double.isNaN(max)) {
                    max = value;
                } else if(!Double.isNaN(value))  {
                    if(value > max) {
                        max = value;
                    }
                }
                result[i] = max;
            }
        }
        return new DoubleArrayColumnView(result);
    }

    @Override
    public double compute(double[] values, int start, int end) {
        double max = Double.NaN;
        for(int i=start;i<end;++i) {
            double value = values[i];
            if(Double.isNaN(max)) {
                max = value;
            } else if(!Double.isNaN(value)) {
                if(value > max) {
                    max = value;
                }
            }
        }
        return max;
    }
}
