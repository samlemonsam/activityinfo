package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.ConstantColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.Date;
import java.util.List;

/**
 * Returns today's current date as {@link org.activityinfo.model.type.time.LocalDate} value
 */
public class TodayFunction extends FormulaFunction implements ColumnFunction {

    public static final TodayFunction INSTANCE = new TodayFunction();

    private TodayFunction() {}

    @Override
    public String getId() {
        return "today";
    }

    @Override
    public String getLabel() {
        return "today";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        if(arguments.size() != 0) {
            throw new FormulaSyntaxException("The TODAY() function doesn't take any arguments");
        }
        return new LocalDate(new Date());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return LocalDateType.INSTANCE;
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        return new ConstantColumnView(numRows, new LocalDate(new Date()).toString());
    }
}
