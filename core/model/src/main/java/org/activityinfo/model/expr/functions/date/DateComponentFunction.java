package org.activityinfo.model.expr.functions.date;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.functions.ColumnFunction;
import org.activityinfo.model.expr.functions.ExprFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;

import java.util.List;

public abstract class DateComponentFunction extends ExprFunction implements ColumnFunction {


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

    protected int apply(String string) {
        return apply(LocalDate.parse(string));
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        checkArity(arguments, 1);

        ColumnView view = arguments.get(0);
        double result[] = new double[view.numRows()];

        for (int i = 0; i < view.numRows(); i++) {
            String date = view.getString(i);
            if(date == null) {
                result[i] = Double.NaN;
            } else {
                result[i] = apply(date);
            }
        }
        return new DoubleArrayColumnView(result);
    }
}

