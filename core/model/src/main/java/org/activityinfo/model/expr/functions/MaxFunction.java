package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.util.List;

/**
 * Computes the maximum value of its arguments
 */
public class MaxFunction extends StatFunction {
    
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
}
