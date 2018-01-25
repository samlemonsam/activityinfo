package org.activityinfo.model.expr.functions;

public class FloorFunction extends RoundingOperator {

    public static final FloorFunction INSTANCE = new FloorFunction();

    public FloorFunction() { super("FLOOR"); }

    @Override
    public double apply(double argument) { return Math.floor(argument); }

}
