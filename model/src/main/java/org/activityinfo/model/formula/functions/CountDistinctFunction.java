package org.activityinfo.model.formula.functions;

import java.util.Arrays;

public class CountDistinctFunction extends StatFunction {

    public static final CountDistinctFunction INSTANCE = new CountDistinctFunction();

    private CountDistinctFunction() {}

    @Override
    public String getId() {
        return "countdistinct";
    }

    @Override
    public String getLabel() {
        return "Count Distinct";
    }

    @Override
    public double compute(double[] values, int start, int end) {
        Arrays.sort(values, start, end);
        int count = 0;
        double lastValue = Double.NaN;
        for (int i = start; i < end; i++) {
            double value = values[i];
            if(!Double.isNaN(value)) {
                if(value != lastValue) {
                    count++;
                }
                lastValue = value;
            }
        }
        return count;
    }
}
