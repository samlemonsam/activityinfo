package org.activityinfo.store.mysql.side;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

class AttributeBuffer implements ValueBuffer {

    private static final Logger LOGGER = Logger.getLogger(AttributeBuffer.class.getName());
    
    private final List<CursorObserver<FieldValue>> observers = Lists.newArrayList();
    private BitSet bitSet = new BitSet();
    private Map<Integer, Integer> attributeMap = Maps.newHashMap();

    /**
     * Cache of enum values for the cases where there is only one item
     */
    private List<EnumValue> singletonValues = Lists.newArrayList();
    private List<ResourceId> itemIds = Lists.newArrayList();

    public AttributeBuffer(EnumType type) {
        for(EnumItem item : type.getValues()) {
            attributeMap.put(CuidAdapter.getLegacyIdFromCuid(item.getId()), singletonValues.size());
            singletonValues.add(new EnumValue(item.getId()));
            itemIds.add(item.getId());
        }
    }

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        boolean value = rs.getBoolean(ATTRIBUTE_VALUE_COLUMN);
        if(value) {
            int id = rs.getInt(ATTRIBUTE_ID_COLUMN);
            if (!rs.wasNull()) {
                Integer index = attributeMap.get(id);
                if(index == null) {
                    LOGGER.info("Unknown attribute " + id);
                } else {
                    bitSet.set(index);
                }
            }
        }
    }

    @Override
    public void next() {
        EnumValue value;
        int cardinality = bitSet.cardinality();
        if(cardinality == 1) {
            // Single item is set, use our cached FieldValues
            int index = bitSet.nextSetBit(0);
            value = singletonValues.get(index);
            
        } else if(cardinality == 0) {
            // No items set, null value
            value = null;

        } else {
            // Multiple items set, construct a new EnumValue
            Set<ResourceId> set = new HashSet<>();
            for (int i = 0; i < itemIds.size(); i++) {
                if(bitSet.get(i)) {
                    set.add(itemIds.get(i));
                }
            }
            value = new EnumValue(set);
        }
        
        for (CursorObserver<FieldValue> observer : observers) {
            observer.onNext(value);
        }
        
        bitSet.clear();
    }

    @Override
    public void done() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }
    
    
}
