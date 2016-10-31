package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordFieldType;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.builders.IdColumnBuilder;
import org.activityinfo.store.query.impl.builders.PrimaryKeySlot;
import org.activityinfo.store.query.impl.builders.RowCountBuilder;
import org.activityinfo.store.query.impl.join.ForeignKeyBuilder;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;

import java.util.*;
import java.util.logging.Logger;

/**
 * Constructs a set of ColumnViews with a single pass over a collection.
 */
public class CollectionScan {

    private static final Logger LOGGER = Logger.getLogger(CollectionScan.class.getName());
    private static final SymbolExpr PK_COLUMN_KEY = new SymbolExpr("@id");

    private final ResourceCollection collection;
    private final ResourceId collectionId;
    private final long cacheVersion;


    private Map<ExprNode, PendingSlot<ColumnView>> columnMap = Maps.newHashMap();
    private Map<String, PendingSlot<ForeignKeyMap>> foreignKeyMap = Maps.newHashMap();

    private Optional<PendingSlot<Integer>> rowCount = Optional.absent();

    public CollectionScan(ResourceCollection collection) {
        this.collection = collection;
        this.collectionId = collection.getFormClass().getId();
        this.cacheVersion = collection.cacheVersion();
    }

    /**
     * Includes the resourceId in the table scan
     *
     * @return a slot that will receive the result when the scan completes
     */
    public Slot<ColumnView> addResourceId() {
        PendingSlot<ColumnView> builder = columnMap.get(PK_COLUMN_KEY);
        if(builder == null) {
            builder = new PendingSlot<>();
            columnMap.put(PK_COLUMN_KEY, builder);
        }
        return builder;
    }

    /**
     * Includes the PrimaryKeyMap structure in the table scan.
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<PrimaryKeyMap> addPrimaryKey() {
        return new PrimaryKeySlot(addResourceId());
    }

    /**
     * Explicitly includes the count of resources in this collection
     * in the table scan.
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<Integer> addCount() {
        if(!rowCount.isPresent()) {
            rowCount = Optional.of(new PendingSlot<Integer>());
        }
        return rowCount.get();
    }

    /**
     * Includes the given field in the table scan.
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<ColumnView> addField(ExprNode fieldExpr) {

        // compose a unique key for this column (we don't want to fetch twice!)
        String columnKey = fieldExpr.toString();

        // if the column's already been added, just return
        if(columnMap.containsKey(fieldExpr)) {
            return columnMap.get(fieldExpr);
        }

        PendingSlot<ColumnView> slot = new PendingSlot<>();
        columnMap.put(fieldExpr, slot);
        return slot;
    }

    private FormField resolveField(FormClass formClass, ExprNode fieldExpr) {
        if(fieldExpr instanceof SymbolExpr) {
            SymbolExpr symbol = (SymbolExpr) fieldExpr;
            ResourceId fieldId = ResourceId.valueOf(symbol.getName());
            return formClass.getField(fieldId);

        } else if(fieldExpr instanceof CompoundExpr) {
            CompoundExpr compound = (CompoundExpr) fieldExpr;
            FormField parent = resolveField(formClass, compound.getValue());
            if(!(parent.getType() instanceof RecordFieldType)) {
                throw new IllegalArgumentException("Cannot resolve " + compound + ": field " + parent.getId() +
                        " is not record-valued.");
            }
            FormClass parentFormClass = ((RecordFieldType) parent.getType()).getFormClass();
            return resolveField(parentFormClass, compound.getField());

        } else {
            throw new UnsupportedOperationException("fieldExpr: " + fieldExpr);
        }
    }


    /**
     * Includes the given foreign key in the table scan
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<ForeignKeyMap> addForeignKey(String fieldName) {

        // create the key builder if it doesn't exist
        PendingSlot<ForeignKeyMap> builder = foreignKeyMap.get(fieldName);
        if(builder == null) {
            builder = new PendingSlot<>();
            foreignKeyMap.put(fieldName, builder);
        }
        return builder;
    }


    public Slot<ForeignKeyMap> addForeignKey(ExprNode referenceField) {
        if(referenceField instanceof SymbolExpr) {
            return addForeignKey(((SymbolExpr) referenceField).getName());
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
    public Set<String> getCacheKeys() {

        // If the collection cannot provide a cache version, then it is not safe to cache columns 
        // from this collection
        if (cacheVersion == 0) {

            LOGGER.severe(this.collectionId + " has zero-valued version.");

            return Collections.emptySet();
        }

        // Otherwise, try to retrieve all of the ColumnView and ForeignKeyMaps we need 
        // from the Memcache service
        Set<String> toFetch = new HashSet<>();
        for (ExprNode fieldId : columnMap.keySet()) {
            toFetch.add(fieldCacheKey(fieldId));
        }
        for (String fieldId : foreignKeyMap.keySet()) {
            toFetch.add(fkCacheKey(fieldId));
        }

        if (rowCount.isPresent()) {
            toFetch.add(rowCountKey());
        }

        return toFetch;
    }


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
                if (rowCount.isPresent()) {
                    rowCount.get().set(view.numRows());
                    rowCount = Optional.absent();
                }
            }
        }

        // And which foreign keys...
        for (String fieldId : Lists.newArrayList(foreignKeyMap.keySet())) {
            ForeignKeyMap map = (ForeignKeyMap) cached.get(fkCacheKey(fieldId));
            if (map != null) {
                foreignKeyMap.get(fieldId).set(map);
                foreignKeyMap.remove(fieldId);
            }
        }

        // Do we need a row count?
        if(rowCount.isPresent()) {
            Integer count = (Integer)cached.get(rowCountKey());
            if(count != null) {
                rowCount.get().set(count);
            }
        }
    }


    /**
     * Executes the tables scan
     */
    public void execute() throws Exception {
        
        // check to see if we still need to hit the database after being populated by the cache
        if(columnMap.isEmpty() && 
           foreignKeyMap.isEmpty() &&
           !rowCount.isPresent()) {
            return;
        }

        // Build the query
        ExprQueryBuilder queryBuilder = new ExprQueryBuilder(collection);

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
        if (rowCount.isPresent() && columnMap.isEmpty()) {
            rowCountBuilder = new RowCountBuilder();
            queryBuilder.addResourceId(rowCountBuilder);
        }

        for (Map.Entry<String, PendingSlot<ForeignKeyMap>> fk : foreignKeyMap.entrySet()) {
            queryBuilder.addField(ResourceId.valueOf(fk.getKey()), new ForeignKeyBuilder(fk.getValue()));
        }

        // Run the query
        Stopwatch stopwatch = Stopwatch.createStarted();
        queryBuilder.execute();

        // Update the row count if hasn't been already loaded from the cache
        if(rowCount.isPresent() && !rowCount.get().isSet()) {
            if(rowCountBuilder != null) {
                rowCount.get().set(rowCountBuilder.getCount());
            } else {
                rowCount.get().set(rowCountFromColumn(columnMap));
            }
        }

        LOGGER.info("Collection scan of " + collection.getFormClass().getId() + " completed in " + stopwatch);
    }
    
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
        for (Map.Entry<String, PendingSlot<ForeignKeyMap>> fk : foreignKeyMap.entrySet()) {
            toPut.put(fkCacheKey(fk.getKey()), fk.getValue().get());
        }
        if(!columnMap.isEmpty()) {
            toPut.put(rowCountKey(), rowCountFromColumn(columnMap));

        } else if(rowCount.isPresent()) {
            toPut.put(rowCountKey(), rowCount.get().get());
        }
        return toPut;
    }
    
    private int rowCountFromColumn(Map<ExprNode, PendingSlot<ColumnView>> columnMap) {
        return columnMap.values().iterator().next().get().numRows();
    }


    private String rowCountKey() {
        return collectionId.asString() + "@" + cacheVersion + "#COUNT";
    }

    private String fieldCacheKey(ExprNode fieldId) {
        return collectionId.asString() + "@" + cacheVersion + "." + fieldId;
    }

    private String fkCacheKey(String fieldId) {
        return collectionId.asString() + "@" + cacheVersion + ".fk." + fieldId;
    }
}
