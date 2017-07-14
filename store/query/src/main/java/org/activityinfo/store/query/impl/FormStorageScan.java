package org.activityinfo.store.query.impl;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.impl.builders.IdColumnBuilder;
import org.activityinfo.store.query.impl.builders.PrimaryKeySlot;
import org.activityinfo.store.query.impl.builders.RowCountBuilder;
import org.activityinfo.store.query.impl.join.ForeignKeyBuilder;
import org.activityinfo.store.query.impl.join.ForeignKey;
import org.activityinfo.store.query.impl.join.ForeignKeyId;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;
import java.util.logging.Logger;

/**
 * Constructs a set of ColumnViews with a single pass over a form's records.
 */
public class FormStorageScan implements FormScan {

    /**
     * The current cache format version prefix.
     *
     * This can be changed to ensure that new versions do not use results cached by earlier versions
     * of ActivityInfo.
     */
    private static final String CACHE_KEY_VERSION = "5:";

    private static final Logger LOGGER = Logger.getLogger(FormStorageScan.class.getName());


    private static final SymbolExpr PK_COLUMN_KEY = new SymbolExpr("@id");

    private final FormStorage storage;
    private final ResourceId formId;
    private final long cacheVersion;

    private Map<ExprNode, PendingSlot<ColumnView>> columnMap = Maps.newHashMap();
    private Map<ForeignKeyId, PendingSlot<ForeignKey>> foreignKeyMap = Maps.newHashMap();

    private PrimaryKeySlot primaryKeySlot = null;
    private PendingSlot<Integer> rowCount = null;


    public FormStorageScan(FormStorage storage) {
        this.storage = storage;
        this.formId = storage.getFormClass().getId();
        this.cacheVersion = storage.cacheVersion();
    }

    /**
     * Includes the resourceId in the table scan
     *
     * @return a slot that will receive the result when the scan completes
     */
    @Override
    public Slot<ColumnView> addResourceId() {
        PendingSlot<ColumnView> column = columnMap.get(PK_COLUMN_KEY);
        if(column == null) {
            column = new PendingSlot<>();
            columnMap.put(PK_COLUMN_KEY, column);
        }
        return column;
    }

    /**
     * Explicitly includes the count of resources in this collection
     * in the table scan.
     *
     * @return a slot where the value can be found after the query completes
     */
    @Override
    public Slot<Integer> addCount() {
        if(rowCount == null) {
            rowCount = new PendingSlot<>();
        }
        return rowCount;
    }

    /**
     * Includes the given field in the table scan.
     *
     * @return a slot where the value can be found after the query completes
     */
    @Override
    public Slot<ColumnView> addField(ExprNode fieldExpr) {

        // if the column's already been added, just return
        if(columnMap.containsKey(fieldExpr)) {
            return columnMap.get(fieldExpr);
        }

        PendingSlot<ColumnView> slot = new PendingSlot<>();
        columnMap.put(fieldExpr, slot);
        return slot;
    }


    /**
     * Includes the given foreign key in the table scan
     *
     * @return a slot where the value can be found after the query completes
     */
    @Override
    public Slot<ForeignKey> addForeignKey(String fieldName, ResourceId rightFormId) {
        // create the key builder if it doesn't exist
        ForeignKeyId fkId = new ForeignKeyId(fieldName, rightFormId);
        PendingSlot<ForeignKey> builder = foreignKeyMap.get(fkId);
        if(builder == null) {
            builder = new PendingSlot<>();
            foreignKeyMap.put(fkId, builder);
        }
        return builder;
    }


    @Override
    public Slot<ForeignKey> addForeignKey(ExprNode referenceField, ResourceId rightFormId) {
        if(referenceField instanceof SymbolExpr) {
            return addForeignKey(((SymbolExpr) referenceField).getName(), rightFormId);
        } else {
            throw new UnsupportedOperationException("TODO: " + referenceField);
        }
    }


    /**
     *
     * Attempts to retrieve as many of the required columns and ForeignKeyMaps as needed from 
     * Memcache
     *
     * @return true if everything could be retrieved from the cache, or false if there remain columns to
     * retrieve.
     */
    @Override
    public Set<String> getCacheKeys() {

        // If the collection cannot provide a cache version, then it is not safe to cache columns 
        // from this collection
        if (cacheVersion == 0) {

            LOGGER.severe(this.formId + " has zero-valued version.");

            return Collections.emptySet();
        }

        // Otherwise, try to retrieve all of the ColumnView and ForeignKeyMaps we need 
        // from the Memcache service
        Set<String> toFetch = new HashSet<>();
        for (ExprNode fieldId : columnMap.keySet()) {
            toFetch.add(fieldCacheKey(fieldId));
        }
        for (ForeignKeyId fk : foreignKeyMap.keySet()) {
            toFetch.add(fkCacheKey(fk));
        }

        if (rowCount != null) {
            toFetch.add(rowCountKey());
        }

        return toFetch;
    }


    @Override
    public void updateFromCache(Map<String, Object> cached) {

        // See which columns we could retrieve from cache
        for (ExprNode fieldId : Lists.newArrayList(columnMap.keySet())) {
            ColumnView view = (ColumnView) cached.get(fieldCacheKey(fieldId));
            if (view != null) {
                // populate the pending result slot with the view from the cache
                columnMap.get(fieldId).set(view);
                // remove this column from the list of columns to fetch
                columnMap.remove(fieldId);

                // resolve the rowCount slot if still needed
                if (rowCount != null) {
                    rowCount.set(view.numRows());
                    rowCount = null;
                }
            }
        }

        // And which foreign keys...
        for (ForeignKeyId keyId : Lists.newArrayList(foreignKeyMap.keySet())) {
            ForeignKey map = (ForeignKey) cached.get(fkCacheKey(keyId));
            if (map != null) {
                foreignKeyMap.get(keyId).set(map);
                foreignKeyMap.remove(keyId);
            }
        }

        // Do we need a row count?
        if(rowCount != null) {
            Integer count = (Integer)cached.get(rowCountKey());
            if(count != null) {
                rowCount.set(count);
            }
        }
    }


    /**
     * Executes the tables scan
     */
    @Override
    public void execute() throws Exception {
        
        // check to see if we still need to hit the database after being populated by the cache
        if(columnMap.isEmpty() && 
           foreignKeyMap.isEmpty() &&
           rowCount == null) {
            return;
        }

        // Build the query
        ExprQueryBuilder queryBuilder = new ExprQueryBuilder(storage);

        for (Map.Entry<ExprNode, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
            if (column.getKey().equals(PK_COLUMN_KEY)) {
                queryBuilder.addResourceId(new IdColumnBuilder(column.getValue()));
            } else {
                queryBuilder.addExpr(column.getKey(), column.getValue());
            }
        }

        // Only add a row count observer IF it has been requested AND 
        // we aren't loading any other columns
        RowCountBuilder rowCountBuilder = null;
        if (rowCount != null && columnMap.isEmpty()) {
            rowCountBuilder = new RowCountBuilder();
            queryBuilder.addResourceId(rowCountBuilder);
        }

        for (Map.Entry<ForeignKeyId, PendingSlot<ForeignKey>> fk : foreignKeyMap.entrySet()) {
            queryBuilder.addField(fk.getKey().getFieldId(), new ForeignKeyBuilder(fk.getKey().getRightFormId(), fk.getValue()));
        }

        // Run the query
        Stopwatch stopwatch = Stopwatch.createStarted();
        queryBuilder.execute();

        // Update the row count if hasn't been already loaded from the cache
        if(rowCount != null && !rowCount.isSet()) {
            if(rowCountBuilder != null) {
                rowCount.set(rowCountBuilder.getCount());
            } else {
                rowCount.set(rowCountFromColumn(columnMap));
            }
        }

        LOGGER.info("Collection scan of " + storage.getFormClass().getId() + " completed in " + stopwatch);
    }
    
    @Override
    public Map<String, Object> getValuesToCache() {
        Map<String, Object> toPut = new HashMap<>();
        for (Map.Entry<ExprNode, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
            ColumnView value;
            try {
                value = column.getValue().get();
            } catch (IllegalStateException e) {
                throw new IllegalStateException(column.getKey().toString(), e);
            }
            toPut.put(fieldCacheKey(column.getKey()), value);
        }
        for (Map.Entry<ForeignKeyId, PendingSlot<ForeignKey>> fk : foreignKeyMap.entrySet()) {
            toPut.put(fkCacheKey(fk.getKey()), fk.getValue().get());
        }
        if(!columnMap.isEmpty()) {
            toPut.put(rowCountKey(), rowCountFromColumn(columnMap));

        } else if(rowCount != null) {
            toPut.put(rowCountKey(), rowCount.get());
        }
        return toPut;
    }
    
    private int rowCountFromColumn(Map<ExprNode, PendingSlot<ColumnView>> columnMap) {
        return columnMap.values().iterator().next().get().numRows();
    }


    private String rowCountKey() {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + "#COUNT";
    }

    private String fieldCacheKey(ExprNode fieldId) {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + "." + fieldId;
    }

    private String fkCacheKey(ForeignKeyId key) {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + ".fk." + key.getFieldName() + "::" + key.getRightFormId();
    }
}
