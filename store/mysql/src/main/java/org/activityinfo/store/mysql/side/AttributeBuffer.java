package org.activityinfo.store.mysql.side;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.service.store.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class AttributeBuffer implements ValueBuffer {

    private final CursorObserver<FieldValue> observer;
    private BitSet bitSet = new BitSet();
    private List<EnumValue> values = Lists.newArrayList();
    private Map<Integer, Integer> attributeMap = Maps.newHashMap();

    public AttributeBuffer(EnumType type, CursorObserver<FieldValue> observer) {
        this.observer = observer;
        for(EnumItem item : type.getValues()) {
            attributeMap.put(CuidAdapter.getLegacyIdFromCuid(item.getId()), values.size());
            values.add(new EnumValue(item.getId()));
        }
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        boolean value = rs.getBoolean(ATTRIBUTE_VALUE_COLUMN);
        if(value) {
            int id = rs.getInt(ATTRIBUTE_ID_COLUMN);
            if (!rs.wasNull()) {
                int index = attributeMap.get(id);
                bitSet.set(index);
            }
        }
    }

    @Override
    public void next() {
        if(bitSet.cardinality() == 1) {
            int index = bitSet.nextSetBit(0);
            observer.onNext(values.get(index));
        } else {
            observer.onNext(null);
        }
        bitSet.clear();
    }

    @Override
    public void done() {
        observer.done();
    }
}
