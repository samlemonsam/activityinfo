package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

public class DivideFunction extends RealValuedBinaryFunction {

    public static final DivideFunction INSTANCE = new DivideFunction();

    private DivideFunction() {
        super("/");
    }

    @Override
    protected double apply(double a, double b) {
        return a / b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        // TODO: we need to properly model units in order to handle this
        return Quantity.UNKNOWN_UNITS;
    }
}
