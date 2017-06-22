package org.activityinfo.store.query.shared;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.columns.IdColumnBuilder;
import org.activityinfo.store.query.shared.columns.RowCountBuilder;
import org.activityinfo.store.query.shared.join.ForeignKeyBuilder;
import org.activityinfo.store.query.shared.join.ForeignKeyMap;
import org.activityinfo.store.spi.ColumnQueryBuilder;

import java.util.*;
import java.util.logging.Logger;

/**
 * Constructs a set of ColumnViews with a single pass over a form's records.
 */
public class FormScan {

    /**
     * The current cache format version prefix.
     *
     * This can be changed to ensure that new versions do not use results cached by earlier versions
     * of ActivityInfo.
     */
    private static final String CACHE_KEY_VERSION = "4:";

    private static final Logger LOGGER = Logger.getLogger(FormScan.class.getName());


    private static final SymbolExpr PK_COLUMN_KEY = new SymbolExpr("@id");

    private final ResourceId formId;
    private final long cacheVersion;
    private final FormClass formClass;

    private Map<ExprNode, PendingSlot<ColumnView>> columnMap = Maps.newHashMap();
    private Map<String, PendingSlot<ForeignKeyMap>> foreignKeyMap = Maps.newHashMap();

    private PendingSlot<Integer> rowCount = null;


    public FormScan(FormClass formClass) {
        this.formId = formClass.getId();
        this.formClass = formClass;
        this.cacheVersion = 0;
    }

    public ResourceId getFormId() {
        return formId;
    }

    /**
     * Includes the resourceId in the table scan
     *
     * @return a slot that will receive the result when the scan completes
     */
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

            LOGGER.severe(this.formId + " has zero-valued version.");

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

        if (rowCount != null) {
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
                if (rowCount != null) {
                    rowCount.set(view.numRows());
                    rowCount = null;
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
        if(rowCount != null) {
            Integer count = (Integer)cached.get(rowCountKey());
            if(count != null) {
                rowCount.set(count);
            }
        }
    }



    /**
     * Prepares a column query based on the requested fields and formulas.
     */
    public void prepare(ColumnQueryBuilder columnQueryBuilder)  {
        
        // check to see if we still need to hit the database after being populated by the cache
        if(columnMap.isEmpty() && 
           foreignKeyMap.isEmpty() &&
           rowCount == null) {
            return;
        }

        // Build the query
        ExprQueryBuilder queryBuilder = new ExprQueryBuilder(formClass, columnQueryBuilder);

        for (Map.Entry<ExprNode, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
            if (column.getKey().equals(PK_COLUMN_KEY)) {
                queryBuilder.addResourceId(new IdColumnBuilder(column.getValue()));
            } else {
                queryBuilder.addExpr(column.getKey(), column.getValue());
            }
        }

        // Only add a row count observer IF it has been requested AND
        // it hasn't been loaded from the cache.
        RowCountBuilder rowCountBuilder = null;
        if (rowCount != null && !rowCount.isSet()) {
            rowCountBuilder = new RowCountBuilder(rowCount);
            queryBuilder.addResourceId(rowCountBuilder);
        }

        for (Map.Entry<String, PendingSlot<ForeignKeyMap>> fk : foreignKeyMap.entrySet()) {
            queryBuilder.addField(ResourceId.valueOf(fk.getKey()), new ForeignKeyBuilder(fk.getValue()));
        }
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

    private String fkCacheKey(String fieldId) {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + ".fk." + fieldId;
    }
}
