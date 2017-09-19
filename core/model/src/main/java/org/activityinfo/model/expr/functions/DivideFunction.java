package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.QuantityType;

public class DivideFunction extends RealValuedFunction {

    public static final DivideFunction INSTANCE = new DivideFunction();

    private DivideFunction() {
        super("/");
    }

    @Override
    protected double apply(double a) {
        throw new IllegalStateException("Illegal unary input to " + getLabel() + "()");
    }

    @Override
    protected double apply(double a, double b) {
        if(b == 0) {
            return Double.NaN;
        }
        return a / b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        // TODO: we need to properly model units in order to handle this
        return "(" + a + ")/(" + b + ")";
    }
}
