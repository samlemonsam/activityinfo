package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.service.store.*;
import org.activityinfo.store.query.impl.builders.*;
import org.activityinfo.store.query.impl.join.ForeignKeyBuilder;
import org.activityinfo.store.query.impl.join.ForeignKeyMap;
import org.activityinfo.store.query.impl.join.PrimaryKeyMap;
import org.activityinfo.store.query.impl.join.PrimaryKeyMapBuilder;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constructs a set of ColumnViews with a single pass over a collection.
 */
public class CollectionScan {

    private static final Logger LOGGER = Logger.getLogger(CollectionScan.class.getName());
    private static final String PK_COLUMN_KEY = "__id";

    private final CollectionAccessor collection;
    private final ColumnQueryBuilder queryBuilder;

    private ColumnCache cache;

    private Optional<PrimaryKeyMapBuilder> primaryKeyMapBuilder = Optional.absent();
    private Map<String, ColumnViewBuilder> columnMap = Maps.newHashMap();
    private Map<String, ForeignKeyBuilder> foreignKeyMap = Maps.newHashMap();

    private Optional<PendingSlot<Integer>> rowCount = Optional.absent();

    public CollectionScan(CollectionAccessor collection, ColumnCache cache) {
        this.collection = collection;
        this.cache = cache;
        this.queryBuilder = collection.newColumnQuery();
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
            queryBuilder.addResourceId(builder);
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

        if(!primaryKeyMapBuilder.isPresent()) {
            PrimaryKeyMapBuilder builder = new PrimaryKeyMapBuilder();
            queryBuilder.addResourceId(builder);
            primaryKeyMapBuilder = Optional.of(builder);
        }
        return primaryKeyMapBuilder.get();
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
                        "by ColumnScanner");

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


        queryBuilder.addField(field.getId(), (CursorObserver<FieldValue>) builder);
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
            queryBuilder.addField(ResourceId.valueOf(fieldName), builder);
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


            // Run the query
            queryBuilder.execute();

            // put to cache
            // TODO: cache.put(collection.getId(), columnMap);

            // update row count
//            if(this.rowCount.isPresent()) {
//                this.rowCount.get().set(rowCount);
//            }
        }
    }

    private boolean resolveFromCache() {
        Map<String, ColumnView> cachedViews = cache.getIfPresent(collection.getFormClass().getId(), columnMap.keySet());

        LOGGER.log(Level.INFO, "Loaded " + cachedViews.size() + " columns from cache");


        if(!cachedViews.isEmpty()) {
            for (Map.Entry<String, ColumnView> cachedEntry : cachedViews.entrySet()) {

                LOGGER.log(Level.INFO, "Loaded " + cachedEntry.getKey() + " from cache");

                ColumnView cachedView = cachedEntry.getValue();
                columnMap.get(cachedEntry.getKey()).setFromCache(cachedView);
                columnMap.remove(cachedEntry.getKey());
            }
            this.rowCount.get().set(cachedViews.values().iterator().next().numRows());
        }

        return columnMap.isEmpty();
    }
}
