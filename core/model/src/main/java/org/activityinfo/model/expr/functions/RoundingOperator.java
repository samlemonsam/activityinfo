package org.activityinfo.model.expr.functions;

import org.activityinfo.model.expr.diagnostic.ArgumentException;
import org.activityinfo.model.expr.diagnostic.InvalidTypeException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

public abstract class RoundingOperator extends UnaryFunctionBase {

    protected RoundingOperator(String name) { super(name); }

    @Override
    public FieldValue apply(FieldValue argument) {
        if(!(argument instanceof Quantity)) {
            throw new InvalidTypeException("Expected QUANTITY value");
        }
        Quantity quantity = (Quantity) argument;
        return new Quantity(apply(quantity.getValue()),quantity.getUnits());
    }

    @Override
    public FieldType resolveUnaryResultType(FieldType argumentType) {
        if (!(argumentType instanceof QuantityType)) {
            throw new ArgumentException(0, "Expected QUANTITY value");
        }
        return new QuantityType();
    }

    @Override
    public ColumnView columnApply(int numRows, ColumnView argument) {
        double[] result = new double[numRows];
        for(int i=0;i<numRows;i++) {
            result[i] = apply(argument.getDouble(i));
        }
        return new DoubleArrayColumnView(result);
    }

    public abstract double apply(double argument);

}
