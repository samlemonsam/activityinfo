package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;

public abstract class ComparisonOperator extends ExprFunction {

    private final String name;

    public ComparisonOperator(String name) {
        this.name = name;
    }

    @Override
    public final String getId() {
        return name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        FieldValue a = arguments.get(0);
        FieldValue b = arguments.get(1);

        // special handling if right operand is constant without units specified: {fieldId}==2.0
        // then constract copy of Quantity without unit information for correct comparison.
        if (a instanceof Quantity && b instanceof Quantity) {
            Quantity aQuantity = (Quantity) a;
            Quantity bQuantity = (Quantity) b;
            if (Quantity.UNKNOWN_UNITS.equals(aQuantity.getUnits()) || Quantity.UNKNOWN_UNITS.equals(bQuantity.getUnits())) {
                a = new Quantity(aQuantity.getValue());
                b = new Quantity(bQuantity.getValue());
            }
        }

        return BooleanFieldValue.valueOf(apply(a, b));

    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }

    protected abstract boolean apply(FieldValue a, FieldValue b);
}
