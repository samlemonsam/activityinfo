package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.shared.columns.FilteredSlot;
import org.activityinfo.store.query.shared.plan.PlanBuilder;
import org.activityinfo.store.query.shared.plan.PlanNode;
import org.activityinfo.store.query.shared.plan.QueryScheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Constructs a set of rows from a given RowSource model.
 *
 * Several row sources may combine to form a single logical table.
 */
public class QueryEvaluator {
    private static final Logger LOGGER = Logger.getLogger(QueryEvaluator.class.getName());

    private final FormTree tree;
    private final FormScanBatch batch;
    private final PlanBuilder planBuilder;
    private final QueryScheduler scheduler;

    public QueryEvaluator(FilterLevel filterLevel, FormTree formTree, FormScanBatch batch) {
        this.tree = formTree;
        this.planBuilder = new PlanBuilder(formTree);
        this.batch = batch;
        this.scheduler = new QueryScheduler(formTree, filterLevel, batch);
    }

    public Slot<ColumnView> evaluateExpression(ExprNode expr) {
        PlanNode planNode = expr.accept(planBuilder);
        Slot<ColumnView> columnView = planNode.accept(scheduler);

        return columnView;
    }


    public Slot<ColumnSet> evaluate(QueryModel model) {

        Slot<TableFilter> filter = filter(model.getFilter());

        final HashMap<String, Slot<ColumnView>> columnViews = Maps.newHashMap();
        for (ColumnModel column : model.getColumns()) {
            Slot<ColumnView> view;
            try {
                view = evaluateExpression(column.getExpression());
            } catch (ExprException e) {
                throw new QuerySyntaxException("Syntax error in column " + column.getId() +
                        " '" + column.getExpression() + "' : " + e.getMessage(), e);
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

                        result = new ColumnSet(commonLength(dataMap), dataMap);
                    }
                    return result;
                }
            };
        }
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

    public Slot<TableFilter> filter(ExprNode filter) {
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

}
