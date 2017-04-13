package org.activityinfo.model.expr.functions;

import com.google.common.base.Preconditions;
import org.activityinfo.model.query.ColumnView;
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

        // Excel-style (sort-of) missing value handling
        // (1) If at least one value is non-missing, however, treat the other as zero
        // (2) If both values are missing, then the result is also missing (NaN)
        // The first case is consistent with Excel, and the second with SQL and R

        Quantity qa = Casting.toQuantity(arguments.get(0));
        Quantity qb = Casting.toQuantity(arguments.get(1));

        if(Double.isNaN(qa.getValue()) && Double.isNaN(qb.getValue())) {
            return new Quantity(Double.NaN);
        }
        double da = toDouble(qa);
        double db = toDouble(qb);

        double value = apply(da, db);
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

    private double toDouble(Quantity quantity) {
        double d = quantity.getValue();
        if(Double.isNaN(d)) {
            return 0d;
        }
        return d;
    }

    @Override
    public final boolean isInfix() {
        return true;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        Preconditions.checkArgument(arguments.size() == 2);
        ColumnView x = arguments.get(0);
        ColumnView y = arguments.get(1);

        double result[] = new double[x.numRows()];
        for (int i = 0; i < result.length; i++) {
            double xd = x.getDouble(i);
            double yd = y.getDouble(i);
            if(Double.isNaN(xd) && Double.isNaN(yd)) {
                result[i] = Double.NaN;
            } else {
                if (Double.isNaN(xd)) {
                    xd = 0;
                }
                if( Double.isNaN(yd)) {
                    yd = 0;
                }
                result[i] = apply(xd, yd);
            }
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
            throw new UnsupportedOperationException("TODO : not implemented, " +
                    "arg 1 : " + argumentTypes.get(0) +
                    "arg 2 : " + argumentTypes.get(1)
            );
        }
    }


    protected abstract double apply(double a, double b);

    protected abstract String applyUnits(String a, String b);
}
