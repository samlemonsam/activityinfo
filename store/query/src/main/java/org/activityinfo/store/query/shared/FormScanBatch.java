package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.query.shared.columns.*;
import org.activityinfo.store.query.shared.join.*;
import org.activityinfo.store.spi.FormVersionProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final ColumnFactory columnFactory;
    private final FormSupervisor supervisor;
    private FormVersionProvider formVersionProvider;
    private final FormClassProvider formClassProvider;


    /**
     * We want to do one pass over each FormClass so
     * keep track of what we need
     */
    private Map<ResourceId, FormScan> tableMap = Maps.newHashMap();

    private Map<FilterKey, Slot<TableFilter>> filterMap = Maps.newHashMap();
    private Map<ResourceId, Slot<TableFilter>> parentFilterMap = Maps.newHashMap();

    private Map<ReferenceJoinKey, ReferenceJoin> joinLinks = new HashMap<>();
    private Map<JoinedColumnKey, JoinedReferenceColumnViewSlot> joinedColumns = new HashMap<>();


    public FormScanBatch(ColumnFactory columnFactory,
                         FormClassProvider formClassProvider, FormVersionProvider formVersionProvider,
                         FormSupervisor supervisor) {
        this.columnFactory = columnFactory;
        this.formVersionProvider = formVersionProvider;
        this.formClassProvider = formClassProvider;
        this.supervisor = supervisor;
    }

    public FormScan getTable(FormClass formClass) {
        return getTable(formClass.getId());
    }

    public FormScan getTable(ResourceId formId) {
        FormScan scan = tableMap.get(formId);
        if(scan == null) {
            scan = new FormScan(columnFactory,
                    formClassProvider.getFormClass(formId),
                    formVersionProvider.getCurrentFormVersion(formId));
            tableMap.put(formId, scan);
        }
        return scan;
    }

    public Iterable<FormScan> getScans() {
        return tableMap.values();
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
    public Slot<ColumnView> addEmptyColumn(FilterLevel filterLevel, FormClass formClass) {
        Slot<Integer> rowCount = addRowCount(filterLevel, formClass);
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
        Slot<ColumnView> parentColumn = addParentColumn(filterLevel, node.getRightFormId());
        Slot<ColumnView> dataColumn = getDataColumn(filterLevel, match.getFormClass().getId(), match.getExpr());

        SubFormJoin join = new SubFormJoin(primaryKey, parentColumn);

        return new JoinedSubFormColumnViewSlot(Collections.singletonList(join), dataColumn);
    }

    private Slot<ColumnView> addParentColumn(FilterLevel filterLevel, ResourceId formId) {
        return getDataColumn(filterLevel, formId, new SymbolExpr("@parent"));
    }

    private ReferenceJoin addJoinLink(FilterLevel filterLevel, JoinNode node) {
        Slot<ForeignKey> foreignKey = addForeignKey(filterLevel, node);
        Slot<PrimaryKeyMap> primaryKey = addPrimaryKey(filterLevel, node.getRightFormId());

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

        return new MemoizedSlot<>(filteredIdSlot, new Function<ColumnView, PrimaryKeyMap>() {
            @Override
            public PrimaryKeyMap apply(ColumnView columnView) {
                return columnFactory.newPrimaryKeyMap(columnView);
            }
        });
    }

    private Slot<ForeignKey> addForeignKey(FilterLevel filterLevel, JoinNode node) {
        Slot<ForeignKey> foreignKey = getTable(node.getLeftFormId()).addForeignKey(node.getReferenceField(), node.getRightFormId());
        Slot<TableFilter> filter = getFilter(filterLevel, node.getLeftFormId());

        return new MemoizedSlot2<>(foreignKey, filter, new BiFunction<ForeignKey, TableFilter, ForeignKey>() {
            @Override
            public ForeignKey apply(ForeignKey foreignKey, TableFilter filter) {
                return filter.apply(foreignKey);
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

        if(!permissions.hasVisibilityFilter()) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        }

        // Otherwise apply per-record permissions
        try {
            FormTreeBuilder formTreeBuilder = new FormTreeBuilder(formClassProvider);
            FormTree formTree = formTreeBuilder.queryTree(formId);

            ExprNode formula = ExprParser.parse(permissions.getViewFilter());
            QueryEvaluator evaluator = new QueryEvaluator(FilterLevel.NONE, formTree, this);
            Slot<ColumnView> filterView = evaluator.evaluateExpression(formula);
            return new MemoizedSlot<>(filterView, new Function<ColumnView, TableFilter>() {
                @Override
                public TableFilter apply(ColumnView columnView) {
                    return new TableFilter(columnView);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse visibility filter", e);
            LOGGER.severe("Error parsing visibility filter '" + permissions.getViewFilter() +
                    " in form " + formId + ": " + e.getMessage() + ". " +
                    "For security reasons, no results will be shown");

            return new PendingSlot<>(TableFilter.NONE_SELECTED);
        }
    }

    private Slot<TableFilter> computeParentFilter(FilterLevel filterLevel, ResourceId formId) {

        if(parentFilterMap.containsKey(formId)) {
            return parentFilterMap.get(formId);
        }

        FormClass formClass = formClassProvider.getFormClass(formId);
        if(!formClass.isSubForm()) {
            return new PendingSlot<>(TableFilter.ALL_SELECTED);
        }

        Slot<PrimaryKeyMap> parentPrimaryKeySlot = addPrimaryKey(filterLevel, formClass.getParentFormId().get());
        Slot<ColumnView> parentId = addParentColumn(FilterLevel.NONE, formId);
        ParentMask filter = new ParentMask(parentPrimaryKeySlot, parentId);

        parentFilterMap.put(formId, filter);

        return filter;
    }
}
