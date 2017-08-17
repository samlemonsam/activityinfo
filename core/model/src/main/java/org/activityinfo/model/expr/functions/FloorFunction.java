package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

public class FloorFunction extends RoundingOperator {

    public static final FloorFunction INSTANCE = new FloorFunction();

    public FloorFunction() { super("FLOOR"); }

    @Override
    public double apply(double argument) { return Math.floor(argument); }

}
