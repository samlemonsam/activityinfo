package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.data.shared.loader.FilterConfig;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.expr.functions.date.DateFunction;
import org.activityinfo.model.type.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Translates between GXT's FilterConfig and ActivityInfo formulas
 */
public class FilterConfigParser {

    public static ExprNode toFormula(ExprNode field, List<FilterConfig> filters) {
        List<ExprNode> filterExprs = new ArrayList<>();
        for (FilterConfig filter : filters) {
            filterExprs.add(toFormula(field, filter));
        }
        return Exprs.allTrue(filterExprs);
    }

    public static ExprNode toFormula(ExprNode field, FilterConfig filter) {
        switch(filter.getType()) {
            case "numeric":
                return toNumericFormula(field, filter);
            case "string":
                return toStringFormula(field, filter);
            case "date":
                return toDateFormula(field, filter);
        }
        throw new UnsupportedOperationException("type: " + filter.getType());
    }


    private static ExprNode toNumericFormula(ExprNode field, FilterConfig filter) {
        return new FunctionCallNode(comparisonFilter(filter), field, numericValue(filter));
    }


    private static ExprNode toStringFormula(ExprNode field, FilterConfig filter) {
        switch (filter.getComparison()) {
            case "contains":
                return stringContains(field, filter);
        }
        throw new UnsupportedOperationException("comparison: " + filter.getComparison());
    }

    private static ExprNode toDateFormula(ExprNode field, FilterConfig filter) {
        return new FunctionCallNode(comparisonFilter(filter),
            field,
            dateValue(filter));
    }

    private static ExprNode dateValue(FilterConfig filter) {
        // GXT serializes the date value as unix time value in milliseconds
        long time = Long.parseLong(filter.getValue());
        Date date = new Date(time);
        LocalDate localDate = new LocalDate(date);

        return new FunctionCallNode(DateFunction.INSTANCE,
            new ConstantExpr(localDate.getYear()),
            new ConstantExpr(localDate.getMonthOfYear()),
            new ConstantExpr(localDate.getDayOfMonth()));
    }

    private static ExprNode stringContains(ExprNode field, FilterConfig filter) {

        ConstantExpr substring = new ConstantExpr(filter.getValue());
        ExprNode string = field;

        return new FunctionCallNode(IsNumberFunction.INSTANCE,
            new FunctionCallNode(SearchFunction.INSTANCE,
                substring,
                string));
    }

    private static ExprFunction comparisonFilter(FilterConfig filter) {
        switch (filter.getComparison()) {
            case "eq":
            case "on":
                return EqualFunction.INSTANCE;
            case "lt":
            case "before":
                return LessFunction.INSTANCE;
            case "gt":
            case "after":
                return GreaterFunction.INSTANCE;
        }
        throw new UnsupportedOperationException("comparison: " + filter.getComparison());
    }

    private static ConstantExpr numericValue(FilterConfig filter) {
        return new ConstantExpr(Double.parseDouble(filter.getValue()));
    }

}
