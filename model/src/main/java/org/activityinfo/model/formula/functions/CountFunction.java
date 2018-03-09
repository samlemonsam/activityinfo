package org.activityinfo.model.formula.functions;

public class CountFunction extends StatFunction {

    public static final CountFunction INSTANCE = new CountFunction();

    private CountFunction() {
    }

    @Override
    public String getId() {
        return "count";
    }

    @Override
    public String getLabel() {
        return "count";
    }

    @Override
    public double compute(double[] values, int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if(!Double.isNaN(values[i])) {
                count++;
            }
        }
        return count;
    }
}
