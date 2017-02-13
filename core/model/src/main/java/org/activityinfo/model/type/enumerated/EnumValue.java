package org.activityinfo.model.type.enumerated;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.HasSetFieldValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnumValue implements FieldValue, HasSetFieldValue {

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

    public Set<EnumItem> getValuesAsItems(EnumType enumType) {
        
        Map<ResourceId, EnumItem> map = new HashMap<>();
        for (EnumItem enumItem : enumType.getValues()) {
            map.put(enumItem.getId(), enumItem);
        }
        
        Set<EnumItem> items = Sets.newHashSet();
        for (final ResourceId resourceId : getResourceIds()) {
            EnumItem item = map.get(resourceId);
            if(item != null) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return EnumType.TYPE_CLASS;
    }

    @Override
    public JsonElement toJsonElement() {
        if(valueIds.isEmpty()) {
            return JsonNull.INSTANCE;
        } else if(valueIds.size() == 1) {
            return new JsonPrimitive(valueIds.iterator().next().asString());
        } else {
            JsonArray array = new JsonArray();
            for (ResourceId valueId : valueIds) {
                array.add(new JsonPrimitive(valueId.asString()));
            }
            return array;
        }
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
