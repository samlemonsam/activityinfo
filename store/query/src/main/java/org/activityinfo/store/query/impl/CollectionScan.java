package org.activityinfo.store.query.impl;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.query.impl.builders.ColumnViewBuilder;
import org.activityinfo.store.query.impl.builders.IdColumnBuilder;
import org.activityinfo.store.query.impl.builders.PrimaryKeySlot;
import org.activityinfo.store.query.impl.builders.ViewBuilderFactory;
import org.activityinfo.store.query.impl.join.ForeignKeyBuilder;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constructs a set of ColumnViews with a single pass over a collection.
 */
public class CollectionScan {

    private static final Logger LOGGER = Logger.getLogger(CollectionScan.class.getName());
    private static final String PK_COLUMN_KEY = "__id";

    private final ResourceCollection collection;
    private final ResourceId collectionId;
    private final long cacheVersion;
    
    private MemcacheService memcacheService;

    private Map<String, ColumnViewBuilder> columnMap = Maps.newHashMap();
    private Map<String, ForeignKeyBuilder> foreignKeyMap = Maps.newHashMap();

    private Optional<PendingSlot<Integer>> rowCount = Optional.absent();

    public CollectionScan(ResourceCollection collection) {
        this.collection = collection;
        this.collectionId = collection.getFormClass().getId();
        this.memcacheService = MemcacheServiceFactory.getMemcacheService();
        this.cacheVersion = collection.cacheVersion();
    }

    /**
     * Includes the resourceId in the table scan
     *
     * @return a slot that will receive the result when the scan completes
     */
    public Slot<ColumnView> addResourceId() {
        IdColumnBuilder builder = (IdColumnBuilder) columnMap.get(PK_COLUMN_KEY);
        if(builder == null) {
            builder = new IdColumnBuilder();
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
    public Slot<ColumnView> addField(FormField field) {

        // ensure that we don't deal with calculated fields at this level
        Preconditions.checkArgument(!(field.getType() instanceof CalculatedFieldType),
                "CollectionScan does not handle calculated fields. This should be taken care of" +
                        "by " + CollectionScan.class.getName());

        // compose a unique key for this column (we don't want to fetch twice!)
        String columnKey = field.getId().asString();

        // if the column's already been added, just return
        if(columnMap.containsKey(columnKey)) {
            return columnMap.get(columnKey);
        }

        // Otherwise create a column builder
        ColumnViewBuilder builder = ViewBuilderFactory.get(field.getType());

        Preconditions.checkNotNull(builder,
                "Column " + columnKey + " has unsupported type: " + field.getType());


        columnMap.put(columnKey, builder);
        return builder;
    }

    /**
     * Includes the given foreign key in the table scan
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<ForeignKeyMap> addForeignKey(String fieldName) {

        // create the key builder if it doesn't exist
        ForeignKeyBuilder builder = foreignKeyMap.get(fieldName);
        if(builder == null) {
            builder = new ForeignKeyBuilder();
            foreignKeyMap.put(fieldName, builder);
        }
        return builder;
    }

    /**
     * Executes the tables scan
     */
    public void execute() throws Exception {

        // First try to retrieve as much as we can from the cache
        if(!resolveFromCache()) {

            // Build the query
            ColumnQueryBuilder queryBuilder = collection.newColumnQuery();

            for (Map.Entry<String, ColumnViewBuilder> column : columnMap.entrySet()) {
                if(column.getKey().equals(PK_COLUMN_KEY)) {
                    queryBuilder.addResourceId((IdColumnBuilder)column.getValue());
                } else {
                    queryBuilder.addField(ResourceId.valueOf(column.getKey()), 
                            (CursorObserver<FieldValue>)column.getValue());
                }
            }

            for (Map.Entry<String, ForeignKeyBuilder> fk : foreignKeyMap.entrySet()) {
                queryBuilder.addField(ResourceId.valueOf(fk.getKey()), fk.getValue());
            }

            // Run the query
            Stopwatch stopwatch = Stopwatch.createStarted();
            queryBuilder.execute();
            
            LOGGER.info("Collection scan of " + collection.getFormClass().getId() + " completed in " + stopwatch);

            // put to cache
            putToCache();
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
    private boolean resolveFromCache() {
        
        // If the collection cannot provide a cache version, then it is not safe to cache columns 
        // from this collection
        if(cacheVersion != 0) {

            // Otherwise, try to retrieve all of the ColumnView and ForeignKeyMaps we need 
            // from the Memcache service
            try {
                Set<String> toFetch = new HashSet<>();
                for (String fieldId : columnMap.keySet()) {
                    toFetch.add(fieldCacheKey(fieldId));
                }
                for (String fieldId : foreignKeyMap.keySet()) {
                    toFetch.add(fkCacheKey(fieldId));
                }

                Map<String, Object> cached = memcacheService.getAll(toFetch);

                LOGGER.log(Level.INFO, "Loaded " + cached.size() + " columns from cache");

                // See which columns we could retrieve from cache
                for (String fieldId : Lists.newArrayList(columnMap.keySet())) {
                    ColumnView view = (ColumnView) cached.get(fieldCacheKey(fieldId));
                    if (view != null) {
                        // populate the pending result slot with the view from the cache
                        columnMap.get(fieldId).setFromCache(view);
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
                        foreignKeyMap.get(fieldId).setFromCache(map);
                        foreignKeyMap.remove(fieldId);
                    }
                }

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while retrieving columns for " + collectionId + " from cache" , e);
            }
        }
        
        return columnMap.isEmpty() &&
                foreignKeyMap.isEmpty() && !rowCount.isPresent();
    
    }

    private void putToCache() {
        try {
            Map<String, Object> toPut = new HashMap<>();
            for (Map.Entry<String, ColumnViewBuilder> column : columnMap.entrySet()) {
                toPut.put(fieldCacheKey(column.getKey()), column.getValue().get());
            }
            for (Map.Entry<String, ForeignKeyBuilder> fk : foreignKeyMap.entrySet()) {
                toPut.put(fkCacheKey(fk.getKey()), fk.getValue().get());
            }
            if(!toPut.isEmpty()) {
                memcacheService.putAll(toPut, Expiration.byDeltaSeconds((int) TimeUnit.HOURS.toSeconds(4)));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception while caching results to memcache for " + collectionId, e);
        }
    }

    private String fieldCacheKey(String fieldId) {
        return collectionId.asString() + "@" + cacheVersion + "." + fieldId;
    }

    private String fkCacheKey(String fieldId) {
        return collectionId.asString() + "@" + cacheVersion + ".fk." + fieldId;
    }
}
