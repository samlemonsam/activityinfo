package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.List;

public abstract class RealValuedBinaryFunction extends ExprFunction implements ColumnFunction {

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

    @Override
    public ColumnView columnApply(List<ColumnView> arguments) {
        Preconditions.checkArgument(arguments.size() == 2);
        ColumnView x = arguments.get(0);
        ColumnView y = arguments.get(1);

        if(x.getType() != ColumnType.NUMBER ||
           y.getType() != ColumnType.NUMBER) {
            return new ConstantColumnView(x.numRows(), Double.NaN);
        }
        double result[] = new double[x.numRows()];
        for (int i = 0; i < result.length; i++) {
            result[i] = apply(x.getDouble(i), y.getDouble(i));
        }
        return new DoubleArrayColumnView(result);
    }

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


    protected abstract double apply(double a, double b);

    protected abstract String applyUnits(String a, String b);
}
