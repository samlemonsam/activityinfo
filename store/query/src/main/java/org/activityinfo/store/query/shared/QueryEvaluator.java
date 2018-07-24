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
package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.functions.BoundingBoxFunction;
import org.activityinfo.model.formula.functions.CoalesceFunction;
import org.activityinfo.model.formula.functions.ColumnFunction;
import org.activityinfo.model.query.*;
import org.activityinfo.model.query.SortModel.Range;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.query.shared.columns.FilteredSlot;
import org.activityinfo.store.query.shared.columns.RelevanceViewMask;
import org.activityinfo.store.spi.PendingSlot;
import org.activityinfo.store.spi.Slot;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Constructs a set of rows from a given RowSource model.
 *
 * Several row sources may combine to form a single logical table.
 */
public class QueryEvaluator {
    private static final Logger LOGGER = Logger.getLogger(QueryEvaluator.class.getName());

    private FormScanBatch batch;
    private FilterLevel filterLevel;
    private FormTree tree;
    private FormClass rootFormClass;

    private final ColumnFormulaVisitor columnVisitor = new ColumnFormulaVisitor();

    private NodeMatcher resolver;

    private Deque<SymbolNode> evaluationStack = new ArrayDeque<>();

    public QueryEvaluator(FilterLevel filterLevel, FormTree formTree, FormScanBatch batch) {
        this.filterLevel = filterLevel;
        this.tree = formTree;
        this.resolver = new NodeMatcher(formTree);
        this.rootFormClass = tree.getRootFormClass();
        this.batch = batch;
    }

    public Slot<ColumnView> evaluateExpression(FormulaNode expr) {
        return expr.accept(columnVisitor);
    }


    private Slot<ColumnView> evaluateExpression(String expression) {
        FormulaNode parsed = FormulaParser.parse(expression);
        return parsed.accept(columnVisitor);
    }


    public Slot<ColumnSet> evaluate(final QueryModel model) {

        Slot<TableFilter> filter = filter(model.getFilter());

        final HashMap<String, Slot<ColumnView>> columnViews = Maps.newHashMap();
        for (ColumnModel column : model.getColumns()) {
            Slot<ColumnView> view;
            try {
                view = evaluateExpression(column.getFormula());
            } catch (FormulaException e) {
                throw new QuerySyntaxException("Syntax error in column " + column.getId() +
                        " '" + column.getFormula() + "' : " + e.getMessage(), e);
            }
            columnViews.put(column.getId(), new FilteredSlot(filter, view));
        }

        if (columnViews.isEmpty()) {
            // Special case for result with no columns -- we need to query the number
            // of rows explicitly

            return new MemoizedSlot<>(batch.addRowCount(FilterLevel.PERMISSIONS, tree.getRootFormId()), new Function<Integer, ColumnSet>() {
                @Override
                public ColumnSet apply(Integer rowCount) {
                    return new ColumnSet(rowCount, Collections.<String, ColumnView>emptyMap());
                }
            });
        } else {
            return new Slot<ColumnSet>() {
                private ColumnSet result = null;

                @Override
                public ColumnSet get() {
                    if (result == null) {
                        // result
                        Map<String, ColumnView> dataMap = Maps.newHashMap();
                        for (Map.Entry<String, Slot<ColumnView>> entry : columnViews.entrySet()) {
                            dataMap.put(entry.getKey(), entry.getValue().get());
                        }

                        ColumnSet dataset = new ColumnSet(commonLength(dataMap), dataMap);
                        if (model.getSortModels().isEmpty()) {
                            result = dataset;
                        } else {
                            result = sort(dataset, model.getSortModels());
                        }
                    }
                    return result;
                }
            };
        }
    }


    private static ColumnSet sort(ColumnSet columnSet, List<SortModel> sortModels) {
        Stack<SortModel> sortModelStack = constructSortModelStack(sortModels);
        int[] sortVector = generateIndexArray(columnSet.getNumRows());
        Range range = new Range(0, columnSet.getNumRows()-1);

        // determine the sort vector of the current columnset, based on the defined sort models
        order(columnSet, sortModelStack, sortVector, range);
        categorize(columnSet, sortModelStack, sortVector, range);

        // return the new sorted column set, with rows reordered by the sort vector
        return sortColumnSet(columnSet, sortVector);
    }

    private static Stack<SortModel> constructSortModelStack(List<SortModel> sortModels) {
        Stack<SortModel> sortCriteria = new Stack<>();
        for (int i=(sortModels.size()-1); i>=0; i--) {
            sortCriteria.push(sortModels.get(i));
        }
        return sortCriteria;
    }

    private static int[] generateIndexArray(int length) {
        int[] array = new int[length];
        for (int i=0; i<length; i++) {
            array[i] = i;
        }
        return array;
    }

    private static void order(ColumnSet columnSet, Stack<SortModel> sortModelStack, int[] sortVector, Range range) {
        if (sortModelStack.empty()) {
            return;
        }

        SortModel sortModel = sortModelStack.pop();
        ColumnView sortColumn = columnSet.getColumnView(sortModel.getField());

        // If SortColumn not returned from query - skip
        if (sortColumn == null) {
            sortModelStack.push(sortModel);
            return;
        }

        // Order on the current sort column
        sortColumn.order(sortVector, sortModel.getDir(), range.getRange());

        // Push sort model back onto stack and return
        sortModelStack.push(sortModel);
    }

    private static void categorize(ColumnSet columnSet, Stack<SortModel> sortModelStack, int[] sortVector, Range range) {
        if (sortModelStack.size() < 2) {
            return;
        }

        SortModel sortModel = sortModelStack.pop();
        ColumnView sortColumn = columnSet.getColumnView(sortModel.getField());

        // If SortColumn not returned from query - skip
        if (sortColumn == null) {
            sortModelStack.push(sortModel);
            return;
        }

        // get encompassing group size and starting position
        int start = range.getStart();
        int end = range.getEnd() + 1;

        // start new category group with first element, stating at encompassing group starting position
        range.resetRange().addToRange(start);

        // step through sortColumn rows
        // group together like members
        // order within each group which has more than one member, after we transition to a new group
        for (int i = start+1; i <= end; i++) {
            if (i != end && Objects.equals(sortColumn.get(sortVector[i-1]), sortColumn.get(sortVector[i]))) {
                // same group - add to group's range
                range.addToRange(i);
            } else if (range.getRangeSize() == 1){
                // transition to new group => no ordering needed as previous group has only 1 member
                range.resetRange().addToRange(i);
            } else {
                // transition to new group => ordering of previous group needed as it has (n > 1) members
                order(columnSet, sortModelStack, sortVector, range);
                categorize(columnSet, sortModelStack, sortVector, range);
                range.resetRange().addToRange(i);
            }
        }

        // Push sort model back onto stack and return
        sortModelStack.push(sortModel);
    }

    private static ColumnSet sortColumnSet(ColumnSet columnSet, int[] sortVector) {
        Map<String, ColumnView> sortedDataMap = Maps.newHashMap();

        for (Map.Entry<String,ColumnView> column : columnSet.getColumns().entrySet()) {
            sortedDataMap.put(column.getKey(), column.getValue().select(sortVector));
        }

        return new ColumnSet(columnSet.getNumRows(), sortedDataMap);
    }


    private static int commonLength(Map<String, ColumnView> dataMap) {
        Iterator<ColumnView> iterator = dataMap.values().iterator();
        if(!iterator.hasNext()) {
            throw new IllegalStateException("Cannot calculate row count from empty column set.");
        }

        int length = iterator.next().numRows();
        while(iterator.hasNext()) {
            if(length != iterator.next().numRows()) {
                logMismatchedRows(dataMap);
                throw new IllegalStateException("Query returned columns of different lengths. See logs for details.");
            }
        }
        return length;
    }

    private static void logMismatchedRows(Map<String, ColumnView> dataMap) {
        StringBuilder message = new StringBuilder();
        message.append("Query returned columns of different lengths:");
        for (Map.Entry<String, ColumnView> entry : dataMap.entrySet()) {
            message.append("\n").append(entry.getKey()).append(" = ").append(entry.getValue().numRows());
        }
        LOGGER.severe(message.toString());
    }

    public Slot<TableFilter> filter(FormulaNode filter) {
        if(filter == null) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        } else {
            return new MemoizedSlot<>(evaluateExpression(filter), new Function<ColumnView, TableFilter>() {
                @Override
                public TableFilter apply(ColumnView columnView) {
                    return new TableFilter(columnView);
                }
            });
        }
    }

    private class ColumnFormulaVisitor implements FormulaVisitor<Slot<ColumnView>> {

        @Override
        public Slot<ColumnView> visitConstant(ConstantNode node) {
            return batch.addConstantColumn(filterLevel, rootFormClass, node.getValue());
        }

        @Override
        public Slot<ColumnView> visitSymbol(SymbolNode symbolNode) {

            // Check for recursion: are we in the process of evaluating
            // this symbol? Trying to do so again will lead to an infinite
            // loop and a StackOverflowException.

            if(evaluationStack.contains(symbolNode)) {
                return batch.addEmptyColumn(filterLevel, rootFormClass);
            }

            evaluationStack.push(symbolNode);

            try {
                Collection<NodeMatch> nodes = resolver.resolveSymbol(symbolNode);
                LOGGER.finer(symbolNode + " matched to " + nodes);
                return addColumn(nodes);

            } finally {
                evaluationStack.pop();
            }
        }

        @Override
        public Slot<ColumnView> visitGroup(GroupNode group) {
            return group.getExpr().accept(this);
        }

        @Override
        public Slot<ColumnView> visitCompoundExpr(CompoundExpr compoundExpr) {
            return addColumn(resolver.resolveCompoundExpr(compoundExpr));
        }

        @Override
        public Slot<ColumnView> visitFunctionCall(final FunctionCallNode call) {
            if(call.getFunction() instanceof ColumnFunction) {
                if(call.getArguments().isEmpty()) {
                    return createNullaryFunctionCall(call);
                } else {
                    return createFunctionCall(call);
                }
            } else if(call.getFunction() instanceof BoundingBoxFunction) {
                FormulaNode geometry = call.getArgument(0);
                Collection<NodeMatch> nodes;
                if(geometry instanceof SymbolNode) {
                    nodes = resolver.resolveSymbol(((SymbolNode) geometry));
                } else if(geometry instanceof CompoundExpr) {
                    nodes = resolver.resolveCompoundExpr((CompoundExpr) geometry);
                } else {
                    throw new QuerySyntaxException("Function " + call.getFunction().getId() + " can only be applied" +
                            " to an argument of type GeoArea.");
                }
                return addColumn(nodes.stream().map(n -> n.withComponent(call.getFunction().getId())).collect(Collectors.toList()));
            } else {
                throw new UnsupportedOperationException("TODO: " + call.getFunction().getId());
            }
        }

        private Slot<ColumnView> createNullaryFunctionCall(final FunctionCallNode call) {
            assert call.getArguments().isEmpty();

            final Slot<Integer> rowCount = batch.addRowCount(filterLevel, rootFormClass);

            return new Slot<ColumnView>() {
                @Override
                public ColumnView get() {
                    return ((ColumnFunction) call.getFunction()).columnApply(rowCount.get(),
                            Collections.<ColumnView>emptyList());
                }
            };

        }

        private FunctionCallSlot createFunctionCall(final FunctionCallNode call) {

            resolver.enterFunction(call.getFunction());

            try {
                final List<Slot<ColumnView>> argumentSlots = Lists.newArrayList();

                for (FormulaNode argument : call.getArguments()) {
                    argumentSlots.add(argument.accept(this));
                }

                return new FunctionCallSlot((ColumnFunction)call.getFunction(), argumentSlots);

            } finally {
                resolver.exitFunction(call.getFunction());
            }

        }

        private Slot<ColumnView> addColumn(Collection<NodeMatch> nodes) {
            // Recursively expand any calculated fields
            List<Slot<ColumnView>> expandedNodes = Lists.newArrayList();
            for (NodeMatch node : nodes) {
                switch (node.getType()) {
                    case ID:
                        expandedNodes.add(batch.addColumn(filterLevel, node));
                        break;
                    case CLASS:
                        throw new UnsupportedOperationException();
                    case FIELD:
                        if (node.isCalculated()) {
                            expandedNodes.add(expandCalculatedField(node));
                        } else {
                            expandedNodes.add(batch.addColumn(filterLevel, node));
                        }
                        break;
                }
            }
            if(expandedNodes.isEmpty()) {
                return batch.addEmptyColumn(filterLevel, rootFormClass);
            } else if(expandedNodes.size() == 1) {
                return expandedNodes.get(0);
            } else {
                return new FunctionCallSlot(CoalesceFunction.INSTANCE, expandedNodes);
            }
        }

        private Slot<ColumnView> expandCalculatedField(NodeMatch node) {
            try {
                Slot<ColumnView> calculation = evaluateExpression(node.getCalculation());

                FormulaNode relevanceFormula = tryParseRelevance(node);
                if(relevanceFormula == null) {
                    return calculation;

                } else {
                    return new MemoizedSlot2<>(calculation, evaluateExpression(relevanceFormula), new BiFunction<ColumnView, ColumnView, ColumnView>() {
                        @Override
                        public ColumnView apply(ColumnView calculation, ColumnView relevance) {
                            return new RelevanceViewMask(calculation, relevance);
                        }
                    });
                }
            } catch (FormulaException e) {
                LOGGER.log(Level.WARNING, "Exception in calculated field " +
                        node.getFormClass().getId() + "." + node.getFieldComponent() + " = " +
                        node.getCalculation() + ": " + e.getMessage(), e);
            
                return batch.addEmptyColumn(filterLevel, node.getFormClass());
            }
        }

        private FormulaNode tryParseRelevance(NodeMatch node) {
            String formula = node.getFieldNode().getField().getRelevanceConditionExpression();
            if(Strings.isNullOrEmpty(formula)) {
                return null;
            }
            try {
                return FormulaParser.parse(formula);
            } catch (FormulaException e) {
                LOGGER.info("Failed to parse relevance condition " + formula);
                return null;
            }
        }

    }

}
