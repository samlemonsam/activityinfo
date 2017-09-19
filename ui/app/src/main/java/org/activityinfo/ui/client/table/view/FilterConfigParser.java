package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.data.shared.loader.FilterConfig;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates between GXT's FilterConfig and ActivityInfo formulas
 */
public class FilterConfigParser {

    public static ExprNode toFormula(List<FilterConfig> filters) {
        List<ExprNode> filterExprs = new ArrayList<>();
        for (FilterConfig filter : filters) {
            filterExprs.add(toFormula(filter));
        }
        return Exprs.allTrue(filterExprs);
    }

    public static ExprNode toFormula(FilterConfig filter) {
        switch(filter.getType()) {
            case "numeric":
                return toNumericFormula(filter);
            case "string":
                return toStringFormula(filter);
        }
        throw new UnsupportedOperationException("type: " + filter.getType());
    }

    private static ExprNode toStringFormula(FilterConfig filter) {
        switch (filter.getComparison()) {
            case "contains":
                return stringContains(filter);
        }
        throw new UnsupportedOperationException("comparison: " + filter.getComparison());
    }

    private static ExprNode stringContains(FilterConfig filter) {

        ConstantExpr substring = new ConstantExpr(filter.getValue());
        SymbolExpr string = field(filter);

        return new FunctionCallNode(IsNumberFunction.INSTANCE,
            new FunctionCallNode(SearchFunction.INSTANCE,
                substring,
                string));
    }

    private static ExprNode toNumericFormula(FilterConfig filter) {
        return new FunctionCallNode(comparsionFunction(filter), field(filter), numericValue(filter));
    }

    private static ExprFunction comparsionFunction(FilterConfig filter) {
        switch (filter.getComparison()) {
            case "eq":
                return EqualFunction.INSTANCE;
            case "lt":
                return LessFunction.INSTANCE;
            case "gt":
                return GreaterFunction.INSTANCE;
        }
        throw new UnsupportedOperationException("comparison: " + filter.getComparison());
    }

    private static ConstantExpr numericValue(FilterConfig filter) {
        return new ConstantExpr(Double.parseDouble(filter.getValue()));
    }

    private static SymbolExpr field(FilterConfig filter) {
        return new SymbolExpr(filter.getField());
    }

}
