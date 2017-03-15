package org.activityinfo.model.expr.functions;

import java.util.Arrays;

/**
 * Computes the median of its arguments.
 */
public class MedianFunction extends StatFunction {

    public static final MedianFunction INSTANCE = new MedianFunction();

    @Override
    public String getId() {
        return "median";
    }

    @Override
    public String getLabel() {
        return "median";
    }

    @Override
    public double compute(double[] values, int start, int end) {

        Arrays.sort(values, start, end);

        // Exclude missing (NaN) values, which are sorted to the end
        // of the array section.
        while(end > start && Double.isNaN(values[end - 1])) {
            end--;
        }

        int length = end - start;
        if(length == 0) {
            return Double.NaN;
        }

        int midpoint = start + (length / 2);
        if(length % 2 == 0) {
            return (values[midpoint] + values[midpoint-1]) / 2.0;
        } else {
            return values[midpoint];
        }
    }
}
