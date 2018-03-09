package org.activityinfo.model.formula.functions;

class MultiplyFunction extends RealValuedFunction {
    public MultiplyFunction() {
        super("*");
    }

    @Override
    protected double apply(double a) {
        throw new IllegalStateException("Illegal unary input to " + getLabel() + "()");
    }

    @Override
    protected double apply(double a, double b) {
        return a * b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        // TODO: we need to properly model units in order to handle this
        return "(" + a + ").(" + b + ")";
    }
}
