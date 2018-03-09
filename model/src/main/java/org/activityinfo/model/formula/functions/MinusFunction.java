package org.activityinfo.model.formula.functions;

import org.activityinfo.model.type.number.QuantityType;

import java.util.Objects;

class MinusFunction extends RealValuedFunction {

    public MinusFunction() {
        super("-");
    }

    @Override
    protected double apply(double a) {
        return -a;
    }

    @Override
    protected double apply(double a, double b) {
        return a - b;
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
