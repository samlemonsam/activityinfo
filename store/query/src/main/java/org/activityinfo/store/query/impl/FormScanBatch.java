package org.activityinfo.store.query.impl;

import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.query.impl.builders.ConstantColumnBuilder;
import org.activityinfo.store.query.impl.builders.FilteredRowCountSlot;
import org.activityinfo.store.query.impl.builders.FilteredSlot;
import org.activityinfo.store.query.impl.builders.PrimaryKeySlot;
import org.activityinfo.store.query.impl.eval.QueryEvaluator;
import org.activityinfo.store.query.impl.join.*;
import org.activityinfo.store.query.shared.JoinNode;
import org.activityinfo.store.query.shared.JoinType;
import org.activityinfo.store.query.shared.NodeMatch;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build a batch of {@code CollectionScans} needed for a column set query.
 *
 * A single query might involve several related tables, and we want
 * to run the table scans in parallel.
 */
public class FormScanBatch {

    private static final Logger LOGGER = Logger.getLogger(FormScanBatch.class.getName());

    private final FormCatalog catalog;
    private final FormScanCache cache;
    private final FormSupervisor supervisor;


    /**
     * We want to do one pass over each FormClass so
     * keep track of what we need
     */
    private Map<ResourceId, FormScan> tableMap = Maps.newHashMap();

    private Map<FilterKey, Slot<TableFilter>> filterMap = Maps.newHashMap();
    private Map<ResourceId, Slot<TableFilter>> parentFilterMap = Maps.newHashMap();

    private Map<ReferenceJoinKey, ReferenceJoin> joinLinks = new HashMap<>();
    private Map<JoinedColumnKey, JoinedReferenceColumnViewSlot> joinedColumns = new HashMap<>();

    private List<Future<Integer>> pendingCachePuts = new ArrayList<>();

    FormScanBatch(FormCatalog formCatalog, FormSupervisor supervisor, FormScanCache cache) {
        this.catalog = formCatalog;
        this.cache = cache;
        this.supervisor = supervisor;
    }

    public FormScan getTable(FormClass formClass) {
        return getTable(formClass.getId());
    }

    public FormScan getTable(ResourceId formId) {
        FormScan scan = tableMap.get(formId);
        if(scan == null) {

            Optional<FormStorage> storage = catalog.getForm(formId);
            if (storage.isPresent()) {
                scan = new FormStorageScan(storage.get());
            } else {
                scan = new EmptyFormScan();
            }
            tableMap.put(formId, scan);
        }
        return scan;
    }

    /**
     * Adds a ResourceId to the batch
     */
    public Slot<ColumnView> addResourceIdColumn(FilterLevel filterLevel, ResourceId formId) {
        return filter(filterLevel, formId, getTable(formId).addResourceId());
    }


    /**
     * Adds a query to the batch for a column derived from a single node within the FormTree, along
     * with any necessary join structures required to join this column to the base table, if the column
     * is nested.
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    public Slot<ColumnView> addColumn(FilterLevel filterLevel, NodeMatch match) {

        if (match.isJoined()) {
            // requires join
            return addJoinedColumn(filterLevel, match);

        } else {
            // simple root column or embedded form
            return getDataColumn(filterLevel, match.getFormClass().getId(), match.getExpr());
        }
    }

    /**
     * Adds a query to the batch for an empty column. It may still be required to hit the data store
     * to find the number of rows.
     */
    public Slot<ColumnView> addEmptyColumn(FormClass formClass) {
        Slot<Integer> rowCount = getTable(formClass.getId()).addCount();
        return new ConstantColumnBuilder(rowCount, null);
    }

    /**
     * Adds a query to the batch for a joined column, which will be joined based on the structure
     * of the FormTree
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    private Slot<ColumnView> addJoinedColumn(FilterLevel filterLevel, NodeMatch match) {

        // For the moment, handle only the simple case of a single subform join
        if(match.getJoins().size() == 1 && match.getJoins().get(0).getType() == JoinType.SUBFORM) {
            return addSubFormJoinedColumn(filterLevel, match);
        }

        // Schedule the links we need to join the node to the base form
        List<ReferenceJoin> links = Lists.newArrayList();
        for (JoinNode joinNode : match.getJoins()) {
            links.add(addJoinLink(filterLevel, joinNode));
        }

        // Schedule the actual column to be joined
        Slot<ColumnView> column;
        switch (match.getType()) {
            case FIELD:
                column = getDataColumn(filterLevel, match.getFormClass().getId(), match.getExpr());
                break;
            case ID:
                column = addResourceIdColumn(filterLevel, match.getFormClass().getId());
                break;
            default:
                throw new UnsupportedOperationException("type: " + match.getType());
        }

        JoinedColumnKey key = new JoinedColumnKey(filterLevel, links, column);
        JoinedReferenceColumnViewSlot slot = joinedColumns.get(key);
        if(slot == null) {
            slot = new JoinedReferenceColumnViewSlot(links, column);
            joinedColumns.put(key, slot);
        }

        return slot;
    }

    private Slot<ColumnView> addSubFormJoinedColumn(FilterLevel filterLevel, NodeMatch match) {
        JoinNode node = match.getJoins().get(0);
        Slot<PrimaryKeyMap> primaryKey =  addPrimaryKey(filterLevel, node.getLeftFormId());
        Slot<ColumnView> parentColumn = addParentColumn(filterLevel, node.getFormClassId());
        Slot<ColumnView> dataColumn = getDataColumn(filterLevel, match.getFormClass().getId(), match.getExpr());

        SubFormJoin join = new SubFormJoin(primaryKey, parentColumn);

        return new JoinedSubFormColumnViewSlot(Collections.singletonList(join), dataColumn);
    }

    private Slot<ColumnView> addParentColumn(FilterLevel filterLevel, ResourceId formId) {
        return getDataColumn(filterLevel, formId, new SymbolExpr("@parent"));
    }

    private ReferenceJoin addJoinLink(FilterLevel filterLevel, JoinNode node) {
        Slot<ForeignKeyMap> foreignKey = addForeignKeyMap(filterLevel, node.getLeftFormId(), node.getReferenceField());
        Slot<PrimaryKeyMap> primaryKey = addPrimaryKey(filterLevel, node.getFormClassId());

        ReferenceJoinKey referenceJoinKey = new ReferenceJoinKey(filterLevel, foreignKey, primaryKey);
        ReferenceJoin joinLink = joinLinks.get(referenceJoinKey);

        if(joinLink == null) {
            joinLink = new ReferenceJoin(foreignKey, primaryKey, node.toString());
            joinLinks.put(referenceJoinKey, joinLink);
        }
        return joinLink;
    }

    private Slot<PrimaryKeyMap> addPrimaryKey(FilterLevel filterLevel, ResourceId formId) {
        Slot<ColumnView> filteredIdSlot = addResourceIdColumn(filterLevel, formId);
        return new PrimaryKeySlot(filteredIdSlot);
    }

    private Slot<ForeignKeyMap> addForeignKeyMap(FilterLevel filterLevel, ResourceId formId, ExprNode referenceField) {
        Slot<ForeignKeyMap> foreignKeyMap = getTable(formId).addForeignKey(referenceField);
        Slot<TableFilter> filter = getFilter(filterLevel, formId);

        return new MemoizedSlot2<>(foreignKeyMap, filter, new BiFunction<ForeignKeyMap, TableFilter, ForeignKeyMap>() {
            @Override
            public ForeignKeyMap apply(ForeignKeyMap foreignKeyMap, TableFilter filter) {
                return filter.apply(foreignKeyMap);
            }
        });
    }

    public Slot<ColumnView> getDataColumn(FilterLevel filterLevel, ResourceId formId, ExprNode fieldExpr) {
        return filter(filterLevel, formId, getTable(formId).addField(fieldExpr));
    }

    /**
     * Adds a request for a "constant" column to the query batch. We don't actually need any data from
     * the form, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FilterLevel filterLevel, FormClass rootFormClass, FieldValue value) {
        return new ConstantColumnBuilder(addRowCount(filterLevel, rootFormClass), value);
    }

    /**
     * Adds a request for a "constant" String column to the query batch. We don't actually need any data from
     * the form, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FilterLevel filterLevel, FormClass rootFormClass, String value) {
        return new ConstantColumnBuilder(addRowCount(filterLevel, rootFormClass), TextValue.valueOf(value));
    }

    public Slot<ColumnView> addExpression(FilterLevel filterLevel, FormClass formClass, ExprNode node) {
        return filter(filterLevel, formClass.getId(), getTable(formClass).addField(node));
    }

    public Slot<Integer> addRowCount(FilterLevel filterLevel, ResourceId formId) {
        Slot<TableFilter> filter = getFilter(filterLevel, formId);
        Slot<Integer> countSlot = getTable(formId).addCount();

        return new FilteredRowCountSlot(countSlot, filter);
    }

    public Slot<Integer> addRowCount(FilterLevel filterLevel, FormClass formClass) {
        return addRowCount(filterLevel, formClass.getId());
    }

    private Slot<ColumnView> filter(FilterLevel filterLevel, ResourceId formId, Slot<ColumnView> viewSlot) {
        return new FilteredSlot(getFilter(filterLevel, formId), viewSlot);
    }

    private Slot<TableFilter> getFilter(FilterLevel filterLevel, ResourceId formId) {

        if(filterLevel == FilterLevel.NONE) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        }

        FilterKey filterKey = new FilterKey(formId, filterLevel);
        if(filterMap.containsKey(filterKey)) {
            return filterMap.get(filterKey);
        }

        Slot<TableFilter> filter = computeFilter(filterLevel, formId);
        filterMap.put(filterKey, filter);

        return filter;
    }

    private Slot<TableFilter> computeFilter(FilterLevel filterLevel, ResourceId formId) {
        final Slot<TableFilter> parentFilter = computeParentFilter(filterLevel, formId);

        if(filterLevel == FilterLevel.BASE) {
            // Only apply parent visibility filter for subforms
            return parentFilter;

        } else {

            final Slot<TableFilter> permissionFilter = computePermissionFilter(formId);

            return new MemoizedSlot2<>(parentFilter, permissionFilter, new BiFunction<TableFilter, TableFilter, TableFilter>() {
                @Override
                public TableFilter apply(TableFilter parentFilter, TableFilter permissionFilter) {
                    return parentFilter.intersection(permissionFilter);
                }
            });
        }
    }

    private Slot<TableFilter> computePermissionFilter(ResourceId formId) {

        FormPermissions permissions = supervisor.getFormPermissions(formId);
        if(!permissions.isVisible()) {
            return new PendingSlot<>(TableFilter.NONE_SELECTED);
        }

        if(!permissions.hasVisiblityFilter()) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        }

        // Otherwise apply per-record permissions
        try {
            FormTreeBuilder formTreeBuilder = new FormTreeBuilder(catalog);
            FormTree formTree = formTreeBuilder.queryTree(formId);

            ExprNode formula = ExprParser.parse(permissions.getVisibilityFilter());
            QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.NONE, formTree, this);
            Slot<ColumnView> filterView = evaluator.evaluateExpression(formula);
            return new MemoizedSlot<>(filterView, new Function<ColumnView, TableFilter>() {
                @Override
                public TableFilter apply(ColumnView columnView) {
                    return new TableFilter(columnView);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse visibility filter", e.getMessage());
            LOGGER.severe("Error parsing visibility filter '" + permissions.getVisibilityFilter() +
                    " in form " + formId + ": " + e.getMessage() + ". " +
                    "For security reasons, no results will be shown");

            return new PendingSlot<>(TableFilter.NONE_SELECTED);
        }
    }

    private Slot<TableFilter> computeParentFilter(FilterLevel filterLevel, ResourceId formId) {

        if(parentFilterMap.containsKey(formId)) {
            return parentFilterMap.get(formId);
        }

        FormClass formClass = catalog.getFormClass(formId);
        if(!formClass.isSubForm()) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        }

        Slot<PrimaryKeyMap> parentPrimaryKeySlot = addPrimaryKey(filterLevel, formClass.getParentFormId().get());
        Slot<ColumnView> parentId = addParentColumn(FilterLevel.NONE, formId);
        ParentMask filter = new ParentMask(parentPrimaryKeySlot, parentId);

        parentFilterMap.put(formId, filter);

        return filter;
    }

    /**
     * Executes the batch
     */
    public void execute() throws Exception {

        // Before hitting the database, retrieve what we can from the cache
        resolveFromCache();
        
        // Now hit the database for anything remaining...
        for(FormScan scan : tableMap.values()) {
            scan.execute();

            // Send separate (async) cache put requests after each collection to avoid
            // having to serialize everything at once and risking OutOfMemoryErrors
            cache(scan);
        }
     }


    /**
     *
     * Attempts to retrieve as many of the required columns from MemCache as possible
     */
    public void resolveFromCache() {


        Set<String> toFetch = new HashSet<>();

        // Collect the keys we need from all enqueued tables
        for (FormScan formScan : tableMap.values()) {
            toFetch.addAll(formScan.getCacheKeys());
        }

        if (!toFetch.isEmpty()) {

            Map<String, Object> cached = cache.getAll(toFetch);

            // Now populate the individual collection scans with what we got back from memcache
            // with a little luck nothing will be left to query directly from the database
            for (FormScan formScan : tableMap.values()) {
                formScan.updateFromCache(cached);
            }
        }
    }


    private void cache(FormScan scan) {
        try {
            Map<String, Object> toPut = scan.getValuesToCache();
            if(!toPut.isEmpty()) {
                Future<Integer> future = cache.enqueuePut(toPut);
                if(!future.isDone()) {
                    pendingCachePuts.add(future);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to start memcache put for " + scan);
        }
    }

    /**
     * Wait for caching to finish, if there is time left in this request.
     */
    public void waitForCachingToFinish() {

        Stopwatch stopwatch = Stopwatch.createStarted();

        int columnCount = 0;
        for (Future<Integer> future : pendingCachePuts) {
            if (!future.isDone()) {
                long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                if (remainingMillis > 100) {
                    try {
                        Integer cachedCount = future.get(remainingMillis - 50, TimeUnit.MILLISECONDS);
                        columnCount += cachedCount;

                    } catch (InterruptedException | TimeoutException e) {
                        LOGGER.warning("Ran out of time while waiting for caching of results to complete.");
                        return;

                    } catch (ExecutionException e) {
                        LOGGER.log(Level.WARNING, "Exception caching results of query", e);
                    }
                }
            }
        }

        LOGGER.info("Waited " + stopwatch + " for " + columnCount + " columns to finish caching.");
    }
}
