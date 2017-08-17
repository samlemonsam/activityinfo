package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.number.Quantity;

public class CeilingFunction extends RoundingOperator {

    public static final CeilingFunction INSTANCE = new CeilingFunction();

    public CeilingFunction() { super("CEIL"); }

    @Override
    public double apply(double argument) { return Math.ceil(argument); }

}
