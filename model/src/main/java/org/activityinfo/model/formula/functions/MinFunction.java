package org.activityinfo.model.formula.functions;

/**
 * Compute the minimum value of its arguments
 */
public class MinFunction extends StatFunction {
    
    public static final MinFunction INSTANCE = new MinFunction();
    
    private MinFunction() {}
    
    @Override
    public String getId() {
        return "min";
    }

    @Override
    public String getLabel() {
        return "min";
    }


    @Override
    public double compute(double[] values, int start, int end) {
        double min = Double.NaN;
        for(int i=start;i<end;++i) {
            double value = values[i];
            if(Double.isNaN(min)) {
                min = value;
            } else if(!Double.isNaN(value)) {
                if(value < min) {
                    min = value;
                }
            }
        }
        return min;    
    }
}
