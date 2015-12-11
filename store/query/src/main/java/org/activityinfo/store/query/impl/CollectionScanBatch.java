package org.activityinfo.store.query.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.builders.ConstantColumnBuilder;
import org.activityinfo.store.query.impl.eval.JoinNode;
import org.activityinfo.store.query.impl.eval.NodeMatch;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;
import org.activityinfo.store.query.impl.join.JoinColumnViewSlot;
import org.activityinfo.store.query.impl.join.JoinLink;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;

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

    /**
     * We want to do one pass over each FormClass so
     * keep track of what we need
     */
    private Map<ResourceId, CollectionScan> tableMap = Maps.newHashMap();


    public CollectionScanBatch(CollectionCatalog store) {
        this.store = store;
    }


    private CollectionScan getTable(FormTree.Node node) {
        return getTable(node.getDefiningFormClass().getId());
    }

    private CollectionScan getTable(FormClass formClass) {
        return getTable(formClass.getId());
    }

    private CollectionScan getTable(ResourceId formClassId) {
        CollectionScan scan = tableMap.get(formClassId);
        if(scan == null) {
            scan = new CollectionScan(store.getCollection(formClassId).get());
            tableMap.put(formClassId, scan);
        }
        return scan;
    }


    /**
     * Adds a ResourceId to the batch
     */
    public Slot<ColumnView> addResourceIdColumn(FormClass classId) {
        return getTable(classId.getId()).addResourceId();
    }


    /**
     * Adds a query to the batch for a column derived from a single node within the FormTree, along
     * with any necessary join structures required to join this column to the base table, if the column
     * is nested.
     *
     * @return a ColumnView Slot that can be used to retrieve the result after the batch
     * has finished executing.
     */
    public Slot<ColumnView> addColumn(NodeMatch match) {

        if (match.isJoined()) {
            // requires join
            return addJoinedColumn(match);

        } else {
            // simple root column or embedded form
            return getDataColumn(match.getField().getRootFormClass(), match.getField());
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
    private Slot<ColumnView> addJoinedColumn(NodeMatch match) {

        // Schedule the links we need to join the node to the base form
        List<JoinLink> links = Lists.newArrayList();
        for (JoinNode joinNode : match.getJoins()) {
            links.add(addJoinLink(joinNode));
        }

        // Schedule the actual column to be joined
        Slot<ColumnView> column;
        switch (match.getType()) {
            case FIELD:
                column = getDataColumn(match.getFormClass(), match.getField());
                break;
            case ID:
                column = getTable(match.getFormClass()).addResourceId();
                break;
            default:
                throw new UnsupportedOperationException("type: " + match.getType());
        }

        return new JoinColumnViewSlot(links, column);
    }

    private JoinLink addJoinLink(JoinNode node) {
        CollectionScan left = getTable(node.getReferenceField());
        CollectionScan right = getTable(node.getFormClassId());

        Slot<ForeignKeyMap> foreignKey = left.addForeignKey(node.getReferenceField().getFieldId().asString());
        Slot<PrimaryKeyMap> primaryKey = right.addPrimaryKey();

        return new JoinLink(foreignKey, primaryKey);
    }

    public Slot<ColumnView> getDataColumn(FormClass formClass, FormTree.Node node) {
        return getTable(formClass.getId()).addField(node.getField());
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
    public Slot<ColumnView> addConstantColumn(FormClass rootFormClass, FieldValue value) {
        return new ConstantColumnBuilder(addRowCount(rootFormClass), value);
    }

    /**
     * Adds a request for a "constant" String column to the query batch. We don't actually need any data from
     * the collection, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FormClass rootFormClass, String value) {
        return new ConstantColumnBuilder(addRowCount(rootFormClass), TextValue.valueOf(value));
    }


    public Slot<ColumnView> addExpression(FormClass formClassId, ExprNode node) {
        //   return getTable(formClassId).addExpression(node);
        throw new UnsupportedOperationException();
    }

    public Slot<Integer> addRowCount(FormClass formClass) {
        return getTable(formClass.getId()).addCount();
    }

}
