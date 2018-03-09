package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.number.QuantityType;

import java.util.Objects;

public class PlusFunction extends RealValuedFunction {

    public static final PlusFunction INSTANCE = new PlusFunction();

    private PlusFunction() {
        super("+");
    }

    @Override
    protected double apply(double a) {
        return +a;
    }

    @Override
    protected double apply(double a, double b) {
        return a + b;
    }

    @Override
    protected String applyUnits(String a, String b) {
        if(Objects.equals(a, b)) {
            return a;
        } else {
            return QuantityType.UNKNOWN_UNITS;
        }
    }
}
