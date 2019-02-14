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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.functions.SumFunction;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.query.shared.columns.*;
import org.activityinfo.store.query.shared.join.*;
import org.activityinfo.store.spi.FieldComponent;
import org.activityinfo.store.spi.FormVersionProvider;
import org.activityinfo.store.spi.PendingSlot;
import org.activityinfo.store.spi.Slot;

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
    public Slot<ColumnView> addRecordIdColumn(FilterLevel filterLevel, ResourceId formId) {
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
            // form label column, id column, simple root column or embedded form
            switch (match.getType()) {
                case FORM_NAME:
                    return addConstantColumn(filterLevel, match.getFormClass(), match.getFormClass().getLabel());
                case RECORD_ID:
                    return addRecordIdColumn(filterLevel, match.getFormClass().getId());
                case FORM_ID:
                    return addConstantColumn(filterLevel, match.getFormClass(), match.getFormClass().getId().asString());
                case FIELD:
                    return getDataColumn(filterLevel, match.getFormClass().getId(), match.getFieldComponent());
                default:
                    throw new UnsupportedOperationException("Type: " + match.getType());
            }
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
                column = getDataColumn(filterLevel, match.getFormClass().getId(), match.getFieldComponent());
                break;
            case RECORD_ID:
                column = addRecordIdColumn(filterLevel, match.getFormClass().getId());
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
        Slot<ColumnView> dataColumn = getDataColumn(filterLevel, match.getFormClass().getId(), match.getFieldComponent());

        SubFormJoin join = new SubFormJoin(primaryKey, parentColumn);

        return new JoinedSubFormColumnViewSlot(Collections.singletonList(join), dataColumn,
                node.getAggregation().or(SumFunction.INSTANCE));
    }

    private Slot<ColumnView> addParentColumn(FilterLevel filterLevel, ResourceId formId) {
        return getDataColumn(filterLevel, formId, new FieldComponent("@parent"));
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
        Slot<ColumnView> filteredIdSlot = addRecordIdColumn(filterLevel, formId);

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

    public Slot<ColumnView> getDataColumn(FilterLevel filterLevel, ResourceId formId, FieldComponent match) {
        return filter(filterLevel, formId, getTable(formId).addField(match));
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

            FormulaNode formula = FormulaParser.parse(permissions.getViewFilter());
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
