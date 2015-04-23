package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

class MultiplyFunction extends RealValuedBinaryFunction {
    public MultiplyFunction() {
        super("*");
    }

    @Override
    protected double apply(double a, double b) {
        return a * b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        // TODO: we need to properly model units in order to handle this
        return Quantity.UNKNOWN_UNITS;
    }
}
