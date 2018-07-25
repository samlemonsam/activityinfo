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
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.BoundingBoxFunction;
import org.activityinfo.model.formula.functions.FormulaFunctions;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.store.query.server.join.ForeignKeyBuilder;
import org.activityinfo.store.query.shared.columns.*;
import org.activityinfo.store.query.shared.join.ForeignKeyId;
import org.activityinfo.store.spi.*;

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
    private static final String CACHE_KEY_VERSION = "17:";

    private static final Logger LOGGER = Logger.getLogger(FormScan.class.getName());


    private static final FieldComponent PK_COLUMN_KEY = new FieldComponent("@id");

    private final ResourceId formId;
    private final long cacheVersion;
    private final FormClass formClass;

    private Map<FieldComponent, PendingSlot<ColumnView>> columnMap = Maps.newHashMap();
    private Map<ForeignKeyId, PendingSlot<ForeignKey>> foreignKeyMap = Maps.newHashMap();

    private PendingSlot<Integer> rowCount = null;
    private ColumnFactory columnFactory;


    public FormScan(ColumnFactory columnFactory, FormClass formClass, long formVersion) {
        this.columnFactory = columnFactory;
        this.formId = formClass.getId();
        this.formClass = formClass;
        this.cacheVersion = formVersion;
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
        return columnMap.computeIfAbsent(PK_COLUMN_KEY, key -> new PendingSlot<>());
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
    public Slot<ColumnView> addField(FieldComponent fieldComponent) {

        // if the column's already been added, just return
        if(columnMap.containsKey(fieldComponent)) {
            return columnMap.get(fieldComponent);
        }


        PendingSlot<ColumnView> slot = new PendingSlot<>();
        columnMap.put(fieldComponent, slot);
        return slot;
    }


    /**
     * Includes the given foreign key in the table scan
     *
     * @return a slot where the value can be found after the query completes
     */
    public Slot<ForeignKey> addForeignKey(String fieldName, ResourceId rightFormId) {
        // create the key builder if it doesn't exist
        ForeignKeyId fkId = new ForeignKeyId(fieldName, rightFormId);
        return foreignKeyMap.computeIfAbsent(fkId, key -> new PendingSlot<>());
    }


    public Slot<ForeignKey> addForeignKey(FormulaNode referenceField, ResourceId rightFormId) {
        if(referenceField instanceof SymbolNode) {
            return addForeignKey(((SymbolNode) referenceField).getName(), rightFormId);
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

            LOGGER.severe(() -> this.formId + " has zero-valued version.");

            return Collections.emptySet();
        }

        // Otherwise, try to retrieve all of the ColumnView and ForeignKeyMaps we need 
        // from the Memcache service
        Set<String> toFetch = new HashSet<>();
        for (FieldComponent fieldId : columnMap.keySet()) {
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


    public void updateFromCache(Map<String, Object> cached) {

        // See which columns we could retrieve from cache
        for (FieldComponent fieldId : Lists.newArrayList(columnMap.keySet())) {
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
     * Prepares a column query based on the requested fields and formulas.
     */
    public void prepare(ColumnQueryBuilder columnQueryBuilder)  {
        
        // check to see if we still need to hit the database after being populated by the cache
        if(allQueriesResolved()) {
            return;
        }

        // Build the query

        for (Map.Entry<FieldComponent, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
            if (column.getKey().equals(PK_COLUMN_KEY)) {
                columnQueryBuilder.addResourceId(new IdColumnBuilder(column.getValue()));
            } else {

                FieldComponent fieldComponent = column.getKey();

                CursorObserver<FieldValue> fieldObserver = buildObserver(column.getKey(), column.getValue());

                columnQueryBuilder.addField(fieldComponent.getFieldId(), fieldObserver);

            }
        }

        // Only add a row count observer IF it has been requested AND
        // it hasn't been loaded from the cache.
        RowCountBuilder rowCountBuilder;
        if (rowCount != null && !rowCount.isSet()) {
            rowCountBuilder = new RowCountBuilder(rowCount);
            columnQueryBuilder.addResourceId(rowCountBuilder);
        }

        for (Map.Entry<ForeignKeyId, PendingSlot<ForeignKey>> fk : foreignKeyMap.entrySet()) {
            columnQueryBuilder.addField(fk.getKey().getFieldId(),
                columnFactory.newForeignKeyBuilder(fk.getKey().getRightFormId(), fk.getValue()));
        }
    }

    private boolean allQueriesResolved() {
        return columnMap.isEmpty() &&
           foreignKeyMap.isEmpty() &&
           rowCount == null;
    }

    public void prepare(ColumnQueryBuilderV2 columnQueryBuilder) {
        // check to see if we still need to hit the database after being populated by the cache
        if(allQueriesResolved()) {
            return;
        }


        // Build the query

        for (Map.Entry<FieldComponent, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
            if (column.getKey().equals(PK_COLUMN_KEY)) {
                columnQueryBuilder.addRecordId(column.getValue());
            } else {
                columnQueryBuilder.addField(column.getKey(), column.getValue());
            }
        }

        // Only add a row count observer IF it has been requested AND
        // it hasn't been loaded from the cache.
        if (rowCount != null && !rowCount.isSet()) {
            columnQueryBuilder.addRowCount(rowCount);
        }

        for (Map.Entry<ForeignKeyId, PendingSlot<ForeignKey>> fk : foreignKeyMap.entrySet()) {
            final ForeignKeyId foreignKey = fk.getKey();
            PendingSlot<ForeignKey> keySlot = fk.getValue();
            PendingSlot<ColumnView> columnSlot = new PendingSlot<ColumnView>() {
                @Override
                public void set(ColumnView column) {
                    ForeignKeyBuilder builder = new ForeignKeyBuilder(foreignKey.getRightFormId(), keySlot);
                    for (int i = 0; i < column.numRows(); i++) {
                        builder.onNext(column.getString(i));
                    }
                    builder.done();
                }
            };
            columnQueryBuilder.addField(
                    new FieldComponent(foreignKey.getFieldName(), foreignKey.getRightFormId().asString()),
                    columnSlot);
        }

        columnQueryBuilder.execute();
    }

    private CursorObserver<FieldValue> buildObserver(FieldComponent fieldComponent, PendingSlot<ColumnView> slot) {
        FormField field = formClass.getField(fieldComponent.getFieldId());

        // Simple case
        if (!fieldComponent.hasComponent()) {
            return ViewBuilderFactory.get(columnFactory, slot, field.getType());
        }

        // Handle field components like latitude, longitdue, etc
        if (field.getType() instanceof GeoPointType) {
            QuantityType coordinateType = new QuantityType("degrees");
            CursorObserver<FieldValue> coordinateObserver = ViewBuilderFactory.get(columnFactory, slot, coordinateType);
            CoordinateReader reader = new CoordinateReader(fieldComponent.getComponent());

            return CursorObservers.transform(reader, coordinateObserver);
        }

        if (field.getType() instanceof EnumType) {
            FieldType resultType = BooleanType.INSTANCE;
            CursorObserver<FieldValue> booleanObserver = ViewBuilderFactory.get(columnFactory, slot, resultType);
            EnumItemReader reader = new EnumItemReader(ResourceId.valueOf(fieldComponent.getComponent()));

            return CursorObservers.transform(reader, booleanObserver);

        } else if(field.getType() instanceof GeoAreaType) {
            QuantityType coordinateType = new QuantityType("degrees");
            BoundingBoxFunction function = (BoundingBoxFunction) FormulaFunctions.get(fieldComponent.getComponent());
            Function<FieldValue, FieldValue> reader = value -> function.apply(Collections.singletonList(value));
            CursorObserver<FieldValue> coordinateObserver = ViewBuilderFactory.get(columnFactory, slot, coordinateType);

            return CursorObservers.transform(reader, coordinateObserver);
        }

        throw new UnsupportedOperationException("Unknown component: " + fieldComponent.getComponent());
    }

    public Map<String, Object> getValuesToCache() {
        Map<String, Object> toPut = new HashMap<>();
        for (Map.Entry<FieldComponent, PendingSlot<ColumnView>> column : columnMap.entrySet()) {
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
    
    private int rowCountFromColumn(Map<FieldComponent, PendingSlot<ColumnView>> columnMap) {
        return columnMap.values().iterator().next().get().numRows();
    }


    private String rowCountKey() {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + "#COUNT";
    }

    private String fieldCacheKey(FieldComponent fieldId) {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + "." + fieldId;
    }

    private String fkCacheKey(ForeignKeyId key) {
        return CACHE_KEY_VERSION + formId.asString() + "@" + cacheVersion + ".fk." + key.getFieldName() + "::" + key.getRightFormId();
    }

    public boolean isEmpty() {
        return allQueriesResolved();
    }
}
