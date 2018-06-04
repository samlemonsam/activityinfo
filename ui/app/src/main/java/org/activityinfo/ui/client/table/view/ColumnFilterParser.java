/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.table.view;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.functions.*;
import org.activityinfo.model.formula.functions.date.DateFunction;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.HasStringValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.*;

/**
 * Translates between GXT's FilterConfig and ActivityInfo formulas.
 */
public class ColumnFilterParser {

    private static final Multimap<Integer, FilterConfig> EMPTY = ImmutableMultimap.of();

    private final Map<FormulaNode, Integer> columnMap = new HashMap<>();

    public ColumnFilterParser() {
    }

    public ColumnFilterParser(List<FormulaNode> columns) {
        for (FormulaNode column : columns) {
            addColumn(column);
        }
    }

    public void addColumn(FormulaNode columnExpr) {
        int columnIndex = columnMap.size();
        columnMap.put(columnExpr, columnIndex);
    }


    public Multimap<Integer, FilterConfig> parseFilter(Optional<String> filterString) {
        if(filterString.isPresent()) {
            return parseFilter(FormulaParser.parse(filterString.get()));
        } else {
            return EMPTY;
        }
    }

    public Multimap<Integer, FilterConfig> parseFilter(FormulaNode filter) {
        Multimap<Integer, FilterConfig> result = HashMultimap.create();
        List<FormulaNode> nodes = Formulas.findBinaryTree(filter, AndFunction.INSTANCE);
        for (FormulaNode node : nodes) {
            if(!parseNode(node, result)) {
                return EMPTY;
            }
        }
        return result;
    }

    private boolean parseNode(FormulaNode node, Multimap<Integer, FilterConfig> result) {
        return parseComparison(node, result) ||
               parseStringContains(node, result) ||
               parseEnumList(node, result);
    }

    private boolean parseComparison(FormulaNode node, Multimap<Integer, FilterConfig> result) {
        if(!(node instanceof FunctionCallNode)) {
            return false;
        }

        // Check that this is a binary
        FunctionCallNode callNode = (FunctionCallNode) node;
        if(callNode.getArgumentCount() != 2) {
            return false;
        }

        // Does this comparison involve one of our fields?
        Integer columnIndex = findColumnIndex(callNode.getArgument(0));
        if(columnIndex == null) {
            return false;
        }

        // Is it compared with a constant value?
        FieldValue value = parseLiteral(callNode.getArgument(1));
        if(value == null) {
            return false;
        }

        FilterConfig config;
        if(value instanceof Quantity) {
            config = numericFilter(callNode, (Quantity) value);
        } else if(value instanceof LocalDate) {
            config = dateFilter(callNode, (LocalDate) value);
        } else {
            return false;
        }

        result.put(columnIndex, config);
        return true;
    }

    private Integer findColumnIndex(FormulaNode expr) {
        FormulaNode columnExpr = expr;
        return columnMap.get(columnExpr);
    }

    /**
     * Tries to parse an expression in the form ISNUMBER(SEARCH(substring, string))
     */
    private boolean parseStringContains(FormulaNode node, Multimap<Integer, FilterConfig> result) {
        if(!(node instanceof FunctionCallNode)) {
            return false;
        }
        FunctionCallNode isNumberCall = ((FunctionCallNode) node);
        if(isNumberCall.getFunction() != IsNumberFunction.INSTANCE) {
            return false;
        }
        FormulaNode isNumberArgument = Formulas.simplify(isNumberCall.getArgument(0));
        if(!(isNumberArgument instanceof FunctionCallNode)) {
            return false;
        }
        FunctionCallNode searchCall = (FunctionCallNode) isNumberArgument;
        if(searchCall.getFunction() != SearchFunction.INSTANCE) {
            return false;
        }
        if(searchCall.getArgumentCount() != 2) {
            return false;
        }
        FieldValue substring = parseLiteral(searchCall.getArgument(0));
        if(!(substring instanceof HasStringValue)) {
            return false;
        }
        FormulaNode columnExpr = searchCall.getArgument(1);
        Integer columnIndex = columnMap.get(columnExpr);
        if(columnIndex == -1) {
            return false;
        }

        FilterConfig filterConfig = new FilterConfigBean();
        filterConfig.setType("string");
        filterConfig.setComparison("contains");
        filterConfig.setValue(((HasStringValue) substring).asString());

        result.put(columnIndex, filterConfig);
        return true;
    }

    /**
     * Parse a list of enum item conditionals in the form (X.A || X.B || X.C)
     */
    private boolean parseEnumList(FormulaNode node, Multimap<Integer, FilterConfig> result) {
        List<FormulaNode> terms = Formulas.findBinaryTree(node, OrFunction.INSTANCE);

        // Ensure that each term is a compound expression in the form X.Y1, X.Y2, X.Y2
        Iterator<FormulaNode> termIt = terms.iterator();
        FormulaNode first = termIt.next();
        if(!(first instanceof CompoundExpr)) {
            return false;
        }
        List<String> enumItemIds = new ArrayList<>();
        CompoundExpr firstCompoundExpr = (CompoundExpr) first;
        Integer firstColumnIndex = findColumnIndex(firstCompoundExpr.getValue());
        if(firstColumnIndex == null) {
            return false;
        }
        enumItemIds.add(firstCompoundExpr.getField().getName());

        // Now check that the remaining terms are also compound expressions with the
        // same field
        while(termIt.hasNext()) {
            FormulaNode term = termIt.next();
            if(!(term instanceof CompoundExpr)) {
                return false;
            }
            CompoundExpr compoundExpr = (CompoundExpr) term;
            Integer columnIndex = findColumnIndex(compoundExpr.getValue());
            if(columnIndex == null || columnIndex != firstColumnIndex) {
                return false;
            }
            enumItemIds.add(compoundExpr.getField().getName());
        }

        // We have a filter that is supported by ListFilter
        FilterConfig listFilter = new FilterConfigBean();
        listFilter.setType("list");
        listFilter.setValue(Joiner.on("::").join(enumItemIds));

        result.put(firstColumnIndex, listFilter);

        return true;
    }


    private FilterConfig numericFilter(FunctionCallNode callNode, Quantity quantity) {
        FilterConfig config = new FilterConfigBean();
        config.setType("numeric");
        config.setComparison(parseNumericComparison(callNode));
        config.setValue(Double.toString(quantity.getValue()));
        return config;
    }


    private FilterConfig dateFilter(FunctionCallNode callNode, LocalDate value) {
        FilterConfig config = new FilterConfigBean();
        config.setType("date");
        config.setComparison(parseDateComparison(callNode));
        config.setValue("" + value.atMidnightInMyTimezone().getTime());
        return config;
    }

    private String parseNumericComparison(FunctionCallNode callNode) {
        if(callNode.getFunction() == LessFunction.INSTANCE) {
            return "lt";
        } else if(callNode.getFunction() == GreaterFunction.INSTANCE) {
            return "gt";
        } else if(callNode.getFunction() == EqualFunction.INSTANCE) {
            return "eq";
        }
        return null;
    }

    private String parseDateComparison(FunctionCallNode callNode) {
        if(callNode.getFunction() == LessFunction.INSTANCE) {
            return "before";
        } else if(callNode.getFunction() == GreaterFunction.INSTANCE) {
            return "after";
        } else if(callNode.getFunction() == EqualFunction.INSTANCE) {
            return "on";
        }
        return null;
    }


    /**
     * If this node is a literal value, for example "abc" or 42.0 or
     * DATE(2017, 1, 1), then return its value. Otherwise return {@code null}.
     */
    private FieldValue parseLiteral(FormulaNode node) {
        if(node instanceof ConstantNode) {
            return ((ConstantNode) node).getValue();
        } else if(node instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) node;
            if(callNode.getFunction() == DateFunction.INSTANCE) {
                FieldValue year = parseLiteral(callNode.getArgument(0));
                FieldValue month = parseLiteral(callNode.getArgument(1));
                FieldValue day = parseLiteral(callNode.getArgument(2));

                if(year != null && month != null || day != null) {
                    return DateFunction.apply(year, month, day);
                }
            }
        }
        return null;
    }


    @VisibleForTesting
    static FormulaNode toFormula(FormulaNode field, FilterConfig filter) {
        switch(filter.getType()) {
            case "numeric":
                return toNumericFormula(field, filter);
            case "string":
                return toStringFormula(field, filter);
            case "date":
                return toDateFormula(field, filter);
            case "list":
                return toListFormula(field, filter);
        }
        throw new UnsupportedOperationException("type: " + filter.getType());
    }


    private static FormulaNode toNumericFormula(FormulaNode field, FilterConfig filter) {
        return new FunctionCallNode(comparisonFilter(filter), field, numericValue(filter));
    }

    private static FormulaNode toStringFormula(FormulaNode field, FilterConfig filter) {
        switch (filter.getComparison()) {
            case "contains":
                return stringContains(field, filter);
        }
        throw new UnsupportedOperationException("comparison: " + filter.getComparison());
    }

    private static FormulaNode toDateFormula(FormulaNode field, FilterConfig filter) {
        return new FunctionCallNode(comparisonFilter(filter),
            field,
            dateValue(filter));
    }


    private static FormulaNode toListFormula(FormulaNode field, FilterConfig filter) {
        // Shockingly, the gxt filterconfig for list is generated by concatenating
        // ids together with "::"

        String[] enumItemIds = filter.getValue().split("::");
        List<FormulaNode> enumConditions = new ArrayList<>();
        for (String enumItemId : enumItemIds) {
            enumConditions.add(new CompoundExpr(field, new SymbolNode(enumItemId)));
        }
        return Formulas.anyTrue(enumConditions);
    }

    private static FormulaNode dateValue(FilterConfig filter) {
        // GXT serializes the date value as unix time value in milliseconds
        long time = Long.parseLong(filter.getValue());
        Date date = new Date(time);
        LocalDate localDate = new LocalDate(date);

        return new FunctionCallNode(DateFunction.INSTANCE,
            new ConstantNode(localDate.getYear()),
            new ConstantNode(localDate.getMonthOfYear()),
            new ConstantNode(localDate.getDayOfMonth()));
    }

    private static FormulaNode stringContains(FormulaNode field, FilterConfig filter) {

        ConstantNode substring = new ConstantNode(Strings.nullToEmpty(filter.getValue()));
        FormulaNode string = field;

        return new FunctionCallNode(IsNumberFunction.INSTANCE,
            new FunctionCallNode(SearchFunction.INSTANCE,
                substring,
                string));
    }

    private static FormulaFunction comparisonFilter(FilterConfig filter) {
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

    private static ConstantNode numericValue(FilterConfig filter) {
        return new ConstantNode(Double.parseDouble(filter.getValue()));
    }

}
