package org.activityinfo.model.formula.functions.date;

import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.functions.Casting;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.formula.functions.FormulaFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * DATE() function implementation.
 *
 * <p>Can you be used to create a LocalDate value, for example: DATE(2015,1,1)</p>
 */
public class DateFunction extends FormulaFunction implements ColumnFunction {

    public static final DateFunction INSTANCE = new DateFunction();

    private DateFunction() {
    }

    @Override
    public String getId() {
        return "DATE";
    }

    @Override
    public String getLabel() {
        return "DATE";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        if(arguments.size() != 3) {
            throw new FormulaSyntaxException("DATE() expects three arguments");
        }
        return apply(arguments.get(0), arguments.get(1), arguments.get(2));
    }

    public static FieldValue apply(FieldValue yearArgument, FieldValue monthArgument, FieldValue dayArgument) {
        double year = Casting.toQuantity(yearArgument).getValue();
        double month = Casting.toQuantity(monthArgument).getValue();
        double day = Casting.toQuantity(dayArgument).getValue();

        return apply(year, month, day);
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        ColumnView year = arguments.get(0);
        ColumnView month = arguments.get(1);
        ColumnView day = arguments.get(2);

        String[] dates = new String[numRows];
        for (int i = 0; i < numRows; i++) {
            dates[i] = apply(
                year.getDouble(i),
                month.getDouble(i),
                day.getDouble(i)).toString();
        }

        return new StringArrayColumnView(dates);
    }



    private static LocalDate apply(double year, double month, double day) {
        return new LocalDate((int)year, (int)month, (int)day);
    }
}
