package org.activityinfo.store.query.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.store.query.impl.builders.ConstantColumnBuilder;
import org.activityinfo.store.query.impl.join.*;
import org.activityinfo.store.query.shared.JoinNode;
import org.activityinfo.store.query.shared.JoinType;
import org.activityinfo.store.query.shared.NodeMatch;

import java.util.*;
import java.util.logging.Logger;

/**
 * Build a batch of {@code CollectionScans} needed for a column set query.
 *
 * A single query might involve several related tables, and we want
 * to run the table scans in parallel.
 */
public class FormScanBatch {

    private static final Logger LOGGER = Logger.getLogger(FormScanBatch.class.getName());

    private final FormScanCache cache;

    private final FormCatalog store;

    /**
     * We want to do one pass over each FormClass so
     * keep track of what we need
     */
    private Map<ResourceId, FormScan> tableMap = Maps.newHashMap();

    private Map<ReferenceJoinKey, ReferenceJoin> joinLinks = new HashMap<>();
    private Map<JoinedColumnKey, JoinedReferenceColumnViewSlot> joinedColumns = new HashMap<>();

    public FormScanBatch(FormCatalog store) {
        this.store = store;
        this.cache = new AppEngineFormScanCache();
    }

    public FormScanBatch(FormCatalog catalog, FormScanCache cache) {
        this.store = catalog;
        this.cache = cache;
    }


    private FormScan getTable(FormTree.Node node) {
        return getTable(node.getDefiningFormClass().getId());
    }

    private FormScan getTable(FormClass formClass) {
        return getTable(formClass.getId());
    }

    private FormScan getTable(ResourceId formClassId) {
        FormScan scan = tableMap.get(formClassId);
        if(scan == null) {
            scan = new FormScan(store.getForm(formClassId).get());
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
            return getDataColumn(match.getFormClass(), match.getExpr());
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

        // For the moment, handle only the simple case of a single subform join
        if(match.getJoins().size() == 1 && match.getJoins().get(0).getType() == JoinType.SUBFORM) {
            return addSubFormJoinedColumn(match);
        }

        // Schedule the links we need to join the node to the base form
        List<ReferenceJoin> links = Lists.newArrayList();
        for (JoinNode joinNode : match.getJoins()) {
            links.add(addJoinLink(joinNode));
        }

        // Schedule the actual column to be joined
        Slot<ColumnView> column;
        switch (match.getType()) {
            case FIELD:
                column = getDataColumn(match.getFormClass(), match.getExpr());
                break;
            case ID:
                column = getTable(match.getFormClass()).addResourceId();
                break;
            default:
                throw new UnsupportedOperationException("type: " + match.getType());
        }

        JoinedColumnKey key = new JoinedColumnKey(links, column);
        JoinedReferenceColumnViewSlot slot = joinedColumns.get(key);
        if(slot == null) {
            slot = new JoinedReferenceColumnViewSlot(links, column);
            joinedColumns.put(key, slot);
        }

        return slot;
    }

    private Slot<ColumnView> addSubFormJoinedColumn(NodeMatch match) {
        JoinNode node = match.getJoins().get(0);
        FormScan left = getTable(node.getLeftFormId());
        FormScan right = getTable(node.getFormClassId());

        Slot<PrimaryKeyMap> primaryKey = left.addPrimaryKey();
        Slot<ColumnView> parentColumn = right.addField(new SymbolExpr("@parent"));
        Slot<ColumnView> dataColumn = getDataColumn(match.getFormClass(), match.getExpr());

        SubFormJoin join = new SubFormJoin(primaryKey, parentColumn);

        return new JoinedSubFormColumnViewSlot(Collections.singletonList(join), dataColumn);
    }

    private ReferenceJoin addJoinLink(JoinNode node) {
        FormScan left = getTable(node.getLeftFormId());
        FormScan right = getTable(node.getFormClassId());

        Slot<ForeignKeyMap> foreignKey = left.addForeignKey(node.getReferenceField());
        Slot<PrimaryKeyMap> primaryKey = right.addPrimaryKey();

        ReferenceJoinKey referenceJoinKey = new ReferenceJoinKey(foreignKey, primaryKey);
        ReferenceJoin joinLink = joinLinks.get(referenceJoinKey);

        if(joinLink == null) {
            joinLink = new ReferenceJoin(foreignKey, primaryKey, node.toString());
            joinLinks.put(referenceJoinKey, joinLink);
        }
        return joinLink;
    }

    public Slot<ColumnView> getDataColumn(FormClass formClass, ExprNode fieldExpr) {
        return getTable(formClass.getId()).addField(fieldExpr);
    }

    /**
     * Adds a request for a "constant" column to the query batch. We don't actually need any data from
     * the form, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FormClass rootFormClass, FieldValue value) {
        return new ConstantColumnBuilder(addRowCount(rootFormClass), value);
    }

    /**
     * Adds a request for a "constant" String column to the query batch. We don't actually need any data from
     * the form, but we do need the row count of the base table.
     * @param rootFormClass
     * @param value
     * @return
     */
    public Slot<ColumnView> addConstantColumn(FormClass rootFormClass, String value) {
        return new ConstantColumnBuilder(addRowCount(rootFormClass), TextValue.valueOf(value));
    }


    public Slot<ColumnView> addExpression(FormClass formClassId, ExprNode node) {
        return getTable(formClassId).addField(node);
    }

    public Slot<Integer> addRowCount(FormClass formClass) {
        return getTable(formClass.getId()).addCount();
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
                cache.enqueuePut(toPut);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to start memcache put for " + scan);
        }
    }

    /**
     * Wait for caching to finish, if there is time left in this request.
     */
    public void waitForCachingToFinish() {
        cache.waitUntilCached();
    }
}
