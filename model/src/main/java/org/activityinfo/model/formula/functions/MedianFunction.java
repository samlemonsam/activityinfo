package org.activityinfo.model.formula.functions;


import com.google.gwt.core.shared.GWT;

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

        if(GWT.isScript()) {
            // Work-around for bug in GWT's Arrays.sort() implementation:
            // NaNs are not handled consistently with the JDK and not sorted
            // to the end of the array. 
            double[] copy = new double[end-start];
            int j = 0;
            for (int i = start; i < end; i++) {
                double value = values[i];
                if(!Double.isNaN(value)) {
                    copy[j++] = value;
                }
            }
            Arrays.sort(copy, 0, j);
            values = copy;
            start = 0;
            end = j;
        } else {
            Arrays.sort(values, start, end);
        }


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
