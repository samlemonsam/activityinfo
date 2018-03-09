package org.activityinfo.model.formula.functions.date;

import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

public class AddDateFunction extends FormulaFunction implements ColumnFunction {

    public static final AddDateFunction INSTANCE = new AddDateFunction();

    private AddDateFunction() {
    }

    @Override
    public String getId() {
        return "adddate";
    }

    @Override
    public String getLabel() {
        return "adddate";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        LocalDate date = (LocalDate) arguments.get(0);
        Quantity days = (Quantity) arguments.get(1);

        return date.plusDays((int) days.getValue());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        ColumnView dateView = arguments.get(0);
        ColumnView daysView = arguments.get(1);
        String[] result = new String[dateView.numRows()];
        for (int i = 0; i < dateView.numRows(); i++) {
            LocalDate date = LocalDate.parse(dateView.getString(i));
            int days = (int)daysView.getDouble(i);
            result[i] = date.plusDays(days).toString();
        }
        return new StringArrayColumnView(result);
    }
}
