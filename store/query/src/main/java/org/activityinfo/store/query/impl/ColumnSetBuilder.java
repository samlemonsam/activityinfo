package org.activityinfo.store.query.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.QuerySyntaxException;
import org.activityinfo.store.query.impl.builders.FilteredSlot;
import org.activityinfo.store.query.impl.eval.QueryEvaluator;
import org.activityinfo.store.spi.FormCatalog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class ColumnSetBuilder {

    public static final Logger LOGGER = Logger.getLogger(ColumnSetBuilder.class.getName());

    private final FormCatalog catalog;
    private final FormTreeBuilder formTreeBuilder;
    private final FormSupervisor supervisor;
    private final FormScanCache cache;

    public ColumnSetBuilder(FormCatalog catalog, FormScanCache cache, FormSupervisor supervisor) {
        this.catalog = catalog;
        this.formTreeBuilder = new FormTreeBuilder(catalog);
        this.cache = cache;
        this.supervisor = supervisor;
    }

    public ColumnSetBuilder(FormCatalog catalog, FormSupervisor supervisor) {
        this(catalog, new AppEngineFormScanCache(), supervisor);
    }

    public FormScanBatch createNewBatch() {
        return new FormScanBatch(catalog, supervisor, cache);
    }

    public ColumnSet build(QueryModel queryModel) {

        // We want to make at most one pass over every collection we need to scan,
        // so first queue up all necessary work before executing
        FormScanBatch batch = createNewBatch();

        // Enqueue the columns we need
        Slot<ColumnSet> columnSet = enqueue(queryModel, batch);

        // Now execute the batch
        try {
            batch.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query batch", e);
        }

        return columnSet.get();
    }


    public Slot<ColumnSet> enqueue(QueryModel table, FormScanBatch batch) {
        ResourceId formId = table.getRowSources().get(0).getRootFormId();
        FormTree tree = formTreeBuilder.queryTree(formId);

        FormClass formClass = tree.getRootFormClass();
        Preconditions.checkNotNull(formClass);

        QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.PERMISSIONS, tree, batch);

        Slot<TableFilter> filter = evaluator.filter(table.getFilter());

        final HashMap<String, Slot<ColumnView>> columnViews = Maps.newHashMap();
        for (ColumnModel column : table.getColumns()) {
            Slot<ColumnView> view;
            try {
                view = evaluator.evaluateExpression(column.getExpression());
            } catch (ExprException e) {
                throw new QuerySyntaxException("Syntax error in column " + column.getId() +
                        " '" + column.getExpression() + "' : " + e.getMessage(), e);
            }
            columnViews.put(column.getId(), new FilteredSlot(filter, view));
        }

        if (columnViews.isEmpty()) {
            // Special case for result with no columns -- we need to query the number
            // of rows explicitly

            return new MemoizedSlot<>(batch.addRowCount(FilterLevel.PERMISSIONS, formClass), new Function<Integer, ColumnSet>() {
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
}
