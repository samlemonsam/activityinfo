package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.BooleanColumnView;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

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

        int[] result = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            if(!Double.isNaN(argument.getDouble(i))) {
                result[i] = 1;
            }
        }

        return new BooleanColumnView(result);
    }
}
