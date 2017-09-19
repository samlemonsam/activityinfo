package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.BitSetColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.BitSet;

public class IsNumberFunction extends UnaryFunctionBase {

    public static final IsNumberFunction INSTANCE = new IsNumberFunction();

    protected IsNumberFunction() {
        super("ISNUMBER");
    }

    @Override
    public FieldType resolveUnaryResultType(FieldType argumentType) {
        return BooleanType.INSTANCE;
    }

    @Override
    public FieldValue apply(FieldValue argument) {
        if(argument instanceof Quantity) {
            double doubleValue = ((Quantity) argument).getValue();
            return BooleanFieldValue.valueOf(!Double.isNaN(doubleValue));
        }
        return BooleanFieldValue.FALSE;
    }

    @Override
    public ColumnView columnApply(int numRows, ColumnView argument) {

        BitSet bitSet = new BitSet();
        for (int i = 0; i < numRows; i++) {
            bitSet.set(i, !Double.isNaN(argument.getDouble(i)));
        }

        return new BitSetColumnView(numRows, bitSet);
    }
}
