package org.activityinfo.model.expr.functions;

import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.DoubleArrayColumnView;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.time.LocalDate;

import java.util.List;

/**
 * Calculates the fraction of the year represented by the number of whole days between two dates
 * (the start_date and the end_date) using the US 30/360 method.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Day_count_convention#30.2F360_US">US 30/360 method</a>
 */
public class YearFracFunction extends ExprFunction implements ColumnFunction {

    public static final YearFracFunction INSTANCE = new YearFracFunction();

    private YearFracFunction() {
    }

    @Override
    public String getId() {
        return "yearfrac";
    }

    @Override
    public String getLabel() {
        return "yearfrac";
    }

    @Override
    public FieldValue apply(List<FieldValue> arguments) {
        if(arguments.size() != 2) {
            throw new ExprSyntaxException("YEARFRAC() requires two arguments");
        }
        LocalDate startDate = (LocalDate) arguments.get(0);
        LocalDate endDate = (LocalDate) arguments.get(1);

        if(startDate == null || endDate == null) {
            return null;
        }

        return new Quantity(compute(startDate, endDate), "years");
    }


    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return new QuantityType("years");
    }

    @VisibleForTesting
    static double compute(LocalDate startDate, LocalDate endDate) {
        int y1 = startDate.getYear();
        int y2 = endDate.getYear();
        int m1 = startDate.getMonthOfYear();
        int m2 = endDate.getMonthOfYear();
        int d1 = startDate.getDayOfMonth();
        int d2 = endDate.getDayOfMonth();

        // If (Date1 is the last day of February) and (Date2 is the last day of February),
        // then change D2 to 30.
        if(lastDayOfFebruary(m1, d1) && lastDayOfFebruary(m2, d2)) {
            d2 = 30;
        }
        if (lastDayOfFebruary(m1, d1)) {
            d1 = 30;
        }
        if(d2 == 31 && (d1 == 30 || d1 == 31)) {
            d2 = 30;
        }
        if(d1 == 31) {
            d1 = 30;
        }

        return Math.abs((360d * (y2-y1) + 30d * (m2-m1) + (d2-d1)) / 360d);
    }

    private static boolean lastDayOfFebruary(int month, int day) {
        return month == 2 && (day == 28 || day == 29);
    }

    @Override
    public ColumnView columnApply(int numRows, List<ColumnView> arguments) {
        if(arguments.size() != 2) {
            throw new ExprSyntaxException("YEARFRAC() requires two arguments");
        }
        ColumnView startView = arguments.get(0);
        ColumnView endView = arguments.get(1);

        double[] result = new double[numRows];

        for (int i = 0; i < numRows; i++) {
            String start = startView.getString(i);
            String end = endView.getString(i);
            if(start == null || end == null) {
                result[i] = Double.NaN;
            } else {
                result[i] = compute(LocalDate.parse(start), LocalDate.parse(end));
            }
        }

        return new DoubleArrayColumnView(result);
    }
}
