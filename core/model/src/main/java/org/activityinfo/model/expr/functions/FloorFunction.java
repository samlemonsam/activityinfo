package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

public class FloorFunction extends RoundingOperator {

    public static final FloorFunction INSTANCE = new FloorFunction();

    public FloorFunction() { super("FLOOR"); }

    @Override
    public Quantity apply(Quantity argument) { return new Quantity(Math.floor(argument.getValue()),argument.getUnits()); }

    @Override
    public double apply(double argument) { return Math.floor(argument); }

}
