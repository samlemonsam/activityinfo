package org.activityinfo.store.query.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.builders.ColumnCombiner;
import org.activityinfo.store.query.impl.builders.ConstantColumnBuilder;
import org.activityinfo.store.query.impl.join.Join;
import org.activityinfo.store.query.impl.join.JoinLink;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;
import org.activityinfo.service.store.CollectionCatalog;

import java.util.List;
import java.util.Map;

/**
 * Build a batch of {@code CollectionScans} needed for a column set query.
 *
 * A single query might involve several related tables, and we want
 * to run the table scans in parallel.
 */
public class CollectionScanBatch {

    private final CollectionCatalog store;
    private ColumnCache columnCache;

    /**
     * We want to do one pass over each FormClass so
     * keep track of what we need
     */
    private Map<ResourceId, CollectionScan> tableMap = Maps.newHashMap();


    public CollectionScanBatch(CollectionCatalog store, ColumnCache columnCache) {
        this.store = store;
        this.columnCache = columnCache;
    }


    private CollectionScan getTable(FormTree.Node node) {
        return getTable(node.getDefiningFormClass());
    }

    private CollectionScan getTable(FormClass formClass) {
        CollectionScan scan = tableMap.get(formClass.getId());
        if(scan == null) {
            scan = new CollectionScan(store.getCollection(formClass.getId()), columnCache);
            tableMap.put(formClass.getId(), scan);
        }
        return scan;
    }


    /**
     * Adds a ResourceId to the batch
     */
    public Slot<ColumnView> addResourceIdColumn(FormClass classId) {
        return getTable(classId).addResourceId();
    }


    /**
     * Adds a query to the batch for a column composed of a several possible nodes within
     * the FormTree.
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    public Slot<ColumnView> addColumn(List<FormTree.Node> nodes) {
        Preconditions.checkArgument(!nodes.isEmpty(), "nodes cannot be empty");

        if(nodes.size() == 1) {
            return addColumn(Iterables.getOnlyElement(nodes));
        } else {
            List<Slot<ColumnView>> sources = Lists.newArrayList();
            for(FormTree.Node node : nodes) {
                sources.add(addColumn(node));
            }
            return new ColumnCombiner(sources);
        }
    }

    /**
     * Adds a query to the batch for a column derived from a single node within the FormTree, along
     * with any necessary join structures required to join this column to the base table, if the column
     * is nested.
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    public Slot<ColumnView> addColumn(FormTree.Node node) {

        if (node.isLinked()) {
            // requires join
            return addNestedColumn(node);

        } else {
            // simple root column or embedded form
            return getDataColumn(node.getRootFormClass(), node);
        }
    }

    /**
     * Adds a query to the batch for an empty column. It may still be required to hit the data store
     * to find the number of rows.
     */
    public Slot<ColumnView> addEmptyColumn(FormClass formClass) {
        Slot<Integer> rowCount = getTable(formClass).addCount();
        return new ConstantColumnBuilder(rowCount, null);
    }

    /**
     * Adds a query to the batch for a nested column, which will be joined based on the structure
     * of the FormTree
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    private Slot<ColumnView> addNestedColumn(FormTree.Node node) {

        // Schedule the links we need to join the node to the base form
        List<FormTree.Node> path = node.getSelfAndAncestors();
        List<JoinLink> links = Lists.newArrayList();
        for(int i=1;i<path.size();++i) {
            FormTree.Node left = path.get(i-1);
            FormTree.Node right = path.get(i);
            links.add(addJoinLink(left, right.getDefiningFormClass()));
        }

        // Schedule the actual column to be joined
        Slot<ColumnView> column = getDataColumn(node.getDefiningFormClass(), node);

        return new Join(links, column);
    }

    private JoinLink addJoinLink(FormTree.Node leftField, FormClass rightForm) {
        CollectionScan left = getTable(leftField);
        CollectionScan right = getTable(rightForm);

        Slot<ForeignKeyMap> foreignKey = left.addForeignKey(leftField.getFieldId().asString());
        Slot<PrimaryKeyMap> primaryKey = right.addPrimaryKey();

        return new JoinLink(foreignKey, primaryKey);
    }

    public Slot<ColumnView> getDataColumn(FormClass formClass, FormTree.Node node) {
        return getTable(formClass).addField(node.getField());
    }
    /**
     * Executes the batch
     */
    public void execute() throws Exception {
        for(CollectionScan scan : tableMap.values()) {
            scan.execute();
        }
    }

    /**
     * Adds a request for a "constant" column to the query batch. We don't actually need any data from
     * the collection, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FormClass rootFormClass, Object value) {
        CollectionScan scan = getTable(rootFormClass);
        Slot<Integer> rows = scan.addCount();

        return new ConstantColumnBuilder(rows, value);
    }

    public Slot<ColumnView> addExpression(FormClass formClassId, ExprNode node) {
     //   return getTable(formClassId).addExpression(node);
        throw new UnsupportedOperationException();
    }
}
