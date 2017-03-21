package org.activityinfo.model.expr.functions;

public class AverageFunction extends StatFunction {

    public static final AverageFunction INSTANCE = new AverageFunction();

    @Override
    public String getId() {
        return "average";
    }

    @Override
    public String getLabel() {
        return "average";
    }

    @Override
    public double compute(double[] values, int start, int end) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            double value = values[i];
            if(!Double.isNaN(value)) {
                sum += value;
                count++;
            }
        }
        return sum / (double)count;
    }
}
