package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

public class CeilingFunction extends RoundingOperator {

    public static final CeilingFunction INSTANCE = new CeilingFunction();

    public CeilingFunction() { super("CEIL"); }

    @Override
    public Quantity apply(Quantity argument) { return new Quantity(Math.ceil(argument.getValue()),argument.getUnits()); }

    @Override
    public double apply(double argument) { return Math.ceil(argument); }

}
