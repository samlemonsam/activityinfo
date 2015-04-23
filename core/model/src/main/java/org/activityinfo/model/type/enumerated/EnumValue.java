package org.activityinfo.model.type.enumerated;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.HasSetFieldValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EnumValue implements FieldValue, IsRecord, HasSetFieldValue {

    public static final EnumValue EMPTY = new EnumValue(Collections.<ResourceId>emptySet());

    private final Set<ResourceId> valueIds;

    public EnumValue(ResourceId valueId) {
        this.valueIds = ImmutableSet.of(valueId);
    }

    public EnumValue(ResourceId... valueIds) {
        this.valueIds = ImmutableSet.copyOf(valueIds);
    }

    public EnumValue(Iterable<ResourceId> valueIds) {
        this.valueIds = ImmutableSet.copyOf(valueIds);
    }

    public Set<ResourceId> getResourceIds() {
        return valueIds;
    }

    public ResourceId getValueId() {
        Preconditions.checkState(valueIds.size() == 1);
        return valueIds.iterator().next();
    }

    @Override
    public Record asRecord() {
        Record record = new Record();
        record.set(TYPE_CLASS_FIELD_NAME, EnumType.TYPE_CLASS.getId());

        if(valueIds.size() == 1) {
            record.set("value", valueIds.iterator().next().asString());
        } else if(valueIds.size() > 1) {
            record.set("value", toStringList(valueIds));
        }
        return record;
    }

    public Set<EnumItem> getValuesAsItems(EnumType enumType) {
        Set<EnumItem> items = Sets.newHashSet();
        for (final ResourceId resourceId : getResourceIds()) {
            for (EnumItem enumItem : enumType.getValues()) {
                if (enumItem.getId().equals(resourceId)) {
                    items.add(enumItem);
                }
            }
        }
        return items;
    }

    private List<String> toStringList(Set<ResourceId> resourceIds) {
        List<String> strings = Lists.newArrayList();
        for(ResourceId resourceId : resourceIds) {
            strings.add(resourceId.asString());
        }
        return strings;
    }

    public static EnumValue fromRecord(Record record) {
        String id = record.isString("value");
        if(id != null) {
            return new EnumValue(ResourceId.valueOf(id));
        }
        id = record.isString("id"); // ugly workaround for inconsistent data that appears on production db
        if(id != null) {
            return new EnumValue(ResourceId.valueOf(id));
        }
        List<String> strings = record.getStringList("value");
        Set<ResourceId> ids = Sets.newHashSet();
        for(String string : strings) {
            ids.add(ResourceId.valueOf(string));
        }
        return new EnumValue(ids);
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return EnumType.TYPE_CLASS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EnumValue that = (EnumValue) o;

        if (!valueIds.equals(that.valueIds)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return valueIds.hashCode();
    }

    @Override
    public String toString() {
        return "EnumValue[" + Joiner.on(", ").join(valueIds) + "]";
    }
}
