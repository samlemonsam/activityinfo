package org.activityinfo.store.query.impl;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
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
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.builders.ConstantColumnBuilder;
import org.activityinfo.store.query.impl.eval.JoinNode;
import org.activityinfo.store.query.impl.eval.JoinType;
import org.activityinfo.store.query.impl.eval.NodeMatch;
import org.activityinfo.store.query.impl.join.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build a batch of {@code CollectionScans} needed for a column set query.
 *
 * A single query might involve several related tables, and we want
 * to run the table scans in parallel.
 */
public class CollectionScanBatch {

    private static final Logger LOGGER = Logger.getLogger(CollectionScanBatch.class.getName());
    
    private AsyncMemcacheService memcacheService = MemcacheServiceFactory.getAsyncMemcacheService();

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

        return new JoinedReferenceColumnViewSlot(links, column);
    }

    private Slot<ColumnView> addSubFormJoinedColumn(NodeMatch match) {
        JoinNode node = match.getJoins().get(0);
        CollectionScan left = getTable(node.getLeftFormId());
        CollectionScan right = getTable(node.getFormClassId());

        Slot<PrimaryKeyMap> primaryKey = left.addPrimaryKey();
        Slot<ColumnView> parentColumn = right.addField(new SymbolExpr("@parent"));
        Slot<ColumnView> dataColumn = getDataColumn(match.getFormClass(), match.getExpr());

        SubFormJoin join = new SubFormJoin(primaryKey, parentColumn);
        
        return new JoinedSubFormColumnViewSlot(Collections.singletonList(join), dataColumn);
    }

    private ReferenceJoin addJoinLink(JoinNode node) {
        CollectionScan left = getTable(node.getLeftFormId());
        CollectionScan right = getTable(node.getFormClassId());

        Slot<ForeignKeyMap> foreignKey = left.addForeignKey(node.getReferenceField());
        Slot<PrimaryKeyMap> primaryKey = right.addPrimaryKey();

        return new ReferenceJoin(foreignKey, primaryKey);
    }

    public Slot<ColumnView> getDataColumn(FormClass formClass, ExprNode fieldExpr) {
        return getTable(formClass.getId()).addField(fieldExpr);
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
        for(CollectionScan scan : tableMap.values()) {
            scan.execute();
        }
        
        // And of course save the results to the cache
        cacheResult();
    }



    /**
     *
     * Attempts to retrieve as many of the required columns from MemCache as possible
     */
    public void resolveFromCache() {


        // Otherwise, try to retrieve all of the ColumnView and ForeignKeyMaps we need 
        // from the Memcache service
        try {
            Set<String> toFetch = new HashSet<>();

            // Collect the keys we need from all enqueued tables
            for (CollectionScan collectionScan : tableMap.values()) {
                toFetch.addAll(collectionScan.getCacheKeys());
            }
            
            if(!toFetch.isEmpty()) {
                // Do a big giant memcache call and rely on appengine to parallelize as
                // needed
                Map<String, Object> cached = memcacheService.getAll(toFetch).get();

                LOGGER.info("Retrieved " + cached.size() + "/" + toFetch.size() + " requested keys from memcache.");

                // Now populate the individual collection scans with what we got back from memcache 
                // with a little luck nothing will be left to query directly from the database
                for (CollectionScan collectionScan : tableMap.values()) {
                    collectionScan.updateFromCache(cached);
                }
            }
            
        } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while retrieving columns from cache" , e);
        }
    }


    private void cacheResult() {
        Map<String, Object> toPut = Maps.newHashMap();

        for (CollectionScan collectionScan : tableMap.values()) {
            toPut.putAll(collectionScan.getValuesToCache());
        }
        
        if(!toPut.isEmpty()) {
            // put asynchronously
            try {
                memcacheService.putAll(toPut, Expiration.byDeltaSeconds(3600), MemcacheService.SetPolicy.SET_ALWAYS);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start memcache put: " + e.getMessage(), e);
            }
        }
    }
}
