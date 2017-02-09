package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;

import java.util.List;

public abstract class DateComponentFunction extends ExprFunction {


    @Override
    public final FieldValue apply(List<FieldValue> arguments) {
        checkArity(arguments, 1);

        FieldValue argument = arguments.get(0);
        if(!(argument instanceof LocalDate)) {
            throw new ExprSyntaxException("Expected date argument");
        }
        LocalDate date = (LocalDate) argument;

        return new Quantity(apply(date), getUnits());
    }

    @Override
    public final FieldType resolveResultType(List<FieldType> argumentTypes) {
        return new QuantityType(getUnits());
    }

    protected abstract String getUnits();

    protected abstract int apply(LocalDate date);
}
