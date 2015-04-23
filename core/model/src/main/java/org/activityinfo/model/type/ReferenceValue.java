package org.activityinfo.model.type;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A Field Value containing the value of {@code ReferenceType} or
 * {@code EnumType}
 */
public class ReferenceValue implements FieldValue, IsRecord, HasSetFieldValue {

    public static final ReferenceValue EMPTY = new ReferenceValue(Collections.<ResourceId>emptySet());

    private final Set<ResourceId> resourceIds;

    public ReferenceValue(ResourceId resourceId) {
        this.resourceIds = ImmutableSet.of(resourceId);
    }

    public ReferenceValue(ResourceId... resourceIds) {
        this.resourceIds = ImmutableSet.copyOf(resourceIds);
    }

    public ReferenceValue(Iterable<ResourceId> resourceIds) {
        this.resourceIds = ImmutableSet.copyOf(resourceIds);
    }

    public Set<ResourceId> getResourceIds() {
        return resourceIds;
    }


    @Override
    public Record asRecord() {
        Record record = new Record();
        record.set(TYPE_CLASS_FIELD_NAME, ReferenceType.TYPE_CLASS.getId());

        if(resourceIds.size() == 1) {
            record.set("value", resourceIds.iterator().next().asString());
        } else if(resourceIds.size() > 1) {
            record.set("value", toStringList(resourceIds));
        }
        return record;
    }

    private List<String> toStringList(Set<ResourceId> resourceIds) {
        List<String> strings = Lists.newArrayList();
        for(ResourceId resourceId : resourceIds) {
            strings.add(resourceId.asString());
        }
        return strings;
    }

    public static ReferenceValue fromRecord(Record record) {
        String id = record.isString("value");
        if(id != null) {
            return new ReferenceValue(ResourceId.valueOf(id));
        }
        List<String> strings = record.getStringList("value");
        Set<ResourceId> ids = Sets.newHashSet();
        for(String string : strings) {
            ids.add(ResourceId.valueOf(string));
        }
        return new ReferenceValue(ids);
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return ReferenceType.TYPE_CLASS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceValue that = (ReferenceValue) o;

        return !(resourceIds != null ? !resourceIds.equals(that.resourceIds) : that.resourceIds != null);

    }

    @Override
    public int hashCode() {
        return resourceIds != null ? resourceIds.hashCode() : 0;
    }

    @Override
    public String toString() {
        return resourceIds.toString();
    }
}
