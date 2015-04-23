package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

public abstract class RealValuedBinaryFunction extends ExprFunction {

    private String name;

    protected RealValuedBinaryFunction(String name) {
        this.name = name;
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        Preconditions.checkState(arguments.size() == 2);
        Quantity qa = Casting.toQuantity(arguments.get(0));
        Quantity qb = Casting.toQuantity(arguments.get(1));

        double value = apply(qa.getValue(), qb.getValue());
        if(Double.isNaN(value)) {
            return new Quantity(Double.NaN);
        } else {
            if(qa.hasUnits() && qb.hasUnits()) {
                return new Quantity(value);
            } else {
                return new Quantity(value, applyUnits(qa.getUnits(), qb.getUnits()));
            }
        }
    }

    protected abstract double apply(double a, double b);

    protected abstract String applyUnits(String a, String b);

    @Override
    public final String getId() {
        return name;
    }

    @Override
    public final String getLabel() {
        return name;
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        Preconditions.checkArgument(argumentTypes.size() == 2);
        if(argumentTypes.get(0) instanceof QuantityType &&
           argumentTypes.get(1) instanceof QuantityType) {

            QuantityType t1 = (QuantityType) argumentTypes.get(0);
            QuantityType t2 = (QuantityType) argumentTypes.get(1);

            return new QuantityType().setUnits(applyUnits(t1.getUnits(), t2.getUnits()));

        } else {
            throw new UnsupportedOperationException("todo");
        }
    }
}
