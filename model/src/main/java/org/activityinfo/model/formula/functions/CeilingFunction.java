package org.activityinfo.model.formula.functions;

public class CeilingFunction extends RoundingOperator {

    public static final CeilingFunction INSTANCE = new CeilingFunction();

    public CeilingFunction() { super("CEIL"); }

    @Override
    public double apply(double argument) { return Math.ceil(argument); }

}
