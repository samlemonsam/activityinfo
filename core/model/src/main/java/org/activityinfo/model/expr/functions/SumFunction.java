package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldValue;

import java.util.List;

/**
 * Computes the sum of its arguments.
 */
public class SumFunction extends StatFunction {

    public static final SumFunction INSTANCE = new SumFunction();

    @Override
    public String getId() {
        return "sum";
    }

    @Override
    public String getLabel() {
        return "sum";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double compute(double[] values, int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            double value = values[i];
            if(!Double.isNaN(value)) {
                sum += value;
            }
        }
        return sum;
    }
}
