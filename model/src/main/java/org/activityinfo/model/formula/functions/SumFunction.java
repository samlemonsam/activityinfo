package org.activityinfo.model.formula.functions;

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
