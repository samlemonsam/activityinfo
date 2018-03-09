package org.activityinfo.model.formula.functions;

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
