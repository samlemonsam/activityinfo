package org.activityinfo.store.query.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
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
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.QuerySyntaxException;
import org.activityinfo.store.query.impl.eval.QueryEvaluator;

import java.util.Map;
import java.util.logging.Logger;

public class ColumnSetBuilder {

    public static final Logger LOGGER = Logger.getLogger(ColumnSetBuilder.class.getName());

    private final CollectionCatalog resourceStore;
    private final FormTreeBuilder formTreeService;
    private Function<ColumnView, ColumnView> filter;
    private Map<String, Slot<ColumnView>> columnViews;
    private Slot<ColumnView> columnForRowCount;

    public ColumnSetBuilder(CollectionCatalog resourceStore) {
        this.resourceStore = resourceStore;
        this.formTreeService = new FormTreeBuilder(resourceStore);
    }

    public ColumnSet build(QueryModel queryModel) {

        // We want to make at most one pass over every collection we need to scan,
        // so first queue up all necessary work before executing
        CollectionScanBatch batch = new CollectionScanBatch(resourceStore);

        // Enqueue the columns we need
        enqueue(queryModel, batch);

        // Now execute the batch
        try {
            batch.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query batch", e);
        }

        return build();
    }


    public void enqueue(QueryModel table, CollectionScanBatch batch) {
        ResourceId classId = table.getRowSources().get(0).getRootFormClass();
        FormTree tree = formTreeService.queryTree(classId);

        FormClass formClass = tree.getRootFormClass();
        Preconditions.checkNotNull(formClass);

        QueryEvaluator evaluator = new QueryEvaluator(tree, formClass, batch);

        filter = evaluator.filter(table.getFilter());

        columnViews = Maps.newHashMap();
        for(ColumnModel column : table.getColumns()) {
            Slot<ColumnView> view;
            try {
                view = evaluator.evaluateExpression(column.getExpression());
            } catch(ExprException e) {
                throw new QuerySyntaxException("Syntax error in column " + column.getId() +
                        " '" + column.getExpression() + "' : " + e.getMessage(), e);
            }
            columnViews.put(column.getId(), view);
        }

        columnForRowCount = null;

        if(columnViews.isEmpty()) {
            // handle empty queries as a special case: we still need the
            // row count so query the id
            columnForRowCount = batch.addResourceIdColumn(formClass);
        }
    }

    public ColumnSet build() {

        // Package the results
        int numRows = -1;
        if(columnForRowCount != null) {
            numRows = columnForRowCount.get().numRows();
        }
        Map<String, ColumnView> dataMap = Maps.newHashMap();
        for (Map.Entry<String, Slot<ColumnView>> entry : columnViews.entrySet()) {
            ColumnView view = filter.apply(entry.getValue().get());

            dataMap.put(entry.getKey(), view);

            if (numRows == -1) {
                numRows = view.numRows();
            } else {
                if (numRows != view.numRows()) {
                    throw new IllegalStateException("Columns are of unequal length: " + dataMap);
                }
            }
        }

        return new ColumnSet(numRows, dataMap);
    }

}
