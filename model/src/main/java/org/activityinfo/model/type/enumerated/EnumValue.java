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
package org.activityinfo.model.type.enumerated;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
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

    public EnumValue(EnumItem item) {
        this(item.getId());
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
    public JsonValue toJson() {
        if(valueIds.isEmpty()) {
            return Json.createNull();
        } else if(valueIds.size() == 1) {
            return Json.create(valueIds.iterator().next().asString());
        } else {
            JsonValue array = Json.createArray();
            for (ResourceId valueId : valueIds) {
                array.add(Json.create(valueId.asString()));
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
