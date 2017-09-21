package org.activityinfo.ui.client.table.view;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.*;
import org.activityinfo.model.expr.functions.date.DateFunction;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.HasStringValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates between GXT's FilterConfig and ActivityInfo formulas.
 */
public class ColumnFilterParser {

    private static final Multimap<Integer, FilterConfig> EMPTY = ImmutableMultimap.of();

    private final Map<ExprNode, Integer> columnMap = new HashMap<>();

    public ColumnFilterParser() {
    }

    public ColumnFilterParser(List<ExprNode> columns) {
        for (ExprNode column : columns) {
            addColumn(column);
        }
    }

    public void addColumn(ExprNode columnExpr) {
        int columnIndex = columnMap.size();
        columnMap.put(columnExpr, columnIndex);
    }


    public Multimap<Integer, FilterConfig> parseFilter(Optional<String> filterString) {
        if(filterString.isPresent()) {
            return parseFilter(ExprParser.parse(filterString.get()));
        } else {
            return EMPTY;
        }
    }

    public Multimap<Integer, FilterConfig> parseFilter(ExprNode filter) {
        Multimap<Integer, FilterConfig> result = HashMultimap.create();
        List<ExprNode> nodes = findConjunctionList(filter);
        for (ExprNode node : nodes) {
            if(!parseNode(node, result)) {
                return EMPTY;
            }
        }
        return result;
    }

    private boolean parseNode(ExprNode node, Multimap<Integer, FilterConfig> result) {
        return parseComparison(node, result) ||
               parseStringContains(node, result);
    }

    private boolean parseComparison(ExprNode node, Multimap<Integer, FilterConfig> result) {
        if(!(node instanceof FunctionCallNode)) {
            return false;
        }

        // Check that this is a binary
        FunctionCallNode callNode = (FunctionCallNode) node;
        if(callNode.getArgumentCount() != 2) {
            return false;
        }

        // Does this comparison involve one of our fields?
        ExprNode columnExpr = callNode.getArgument(0);
        Integer columnIndex = columnMap.get(columnExpr);
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

    /**
     * Tries to parse an expression in the form ISNUMBER(SEARCH(substring, string))
     */
    private boolean parseStringContains(ExprNode node, Multimap<Integer, FilterConfig> result) {
        if(!(node instanceof FunctionCallNode)) {
            return false;
        }
        FunctionCallNode isNumberCall = ((FunctionCallNode) node);
        if(isNumberCall.getFunction() != IsNumberFunction.INSTANCE) {
            return false;
        }
        ExprNode isNumberArgument = simplify(isNumberCall.getArgument(0));
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
        ExprNode columnExpr = searchCall.getArgument(1);
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
    private FieldValue parseLiteral(ExprNode node) {
        if(node instanceof ConstantExpr) {
            return ((ConstantExpr) node).getValue();
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

    /**
     * Tries to decompose the formula into a list of conjunctions (A && B).
     */
    @VisibleForTesting
    static List<ExprNode> findConjunctionList(ExprNode rootNode) {
        List<ExprNode> list = new ArrayList<>();
        findConjunctionList(rootNode, list);

        return list;
    }

    private static void findConjunctionList(ExprNode node, List<ExprNode> list) {

        // Unwrap group expressions ((A))
        node = simplify(node);

        if(isConjunction(node)) {
            // If this expression is in the form A && B, then descend
            // recursively
            FunctionCallNode callNode = (FunctionCallNode) node;
            findConjunctionList(callNode.getArgument(0), list);
            findConjunctionList(callNode.getArgument(1), list);

        } else {
            // If not a conjunction, then add this node to the list
            list.add(node);
        }
    }

    private static ExprNode simplify(ExprNode node) {
        while(node instanceof GroupExpr) {
            node = ((GroupExpr) node).getExpr();
        }
        return node;
    }

    private static boolean isConjunction(ExprNode node) {
        if(node instanceof FunctionCallNode) {
            FunctionCallNode callNode = (FunctionCallNode) node;
            return callNode.getFunction() == AndFunction.INSTANCE;
        }
        return false;
    }

}
