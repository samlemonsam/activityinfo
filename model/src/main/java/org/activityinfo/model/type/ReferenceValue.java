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
package org.activityinfo.model.type;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A Field Value containing the value of {@code ReferenceType} or
 * {@code EnumType}
 */
public class ReferenceValue implements FieldValue {

    public static final ReferenceValue EMPTY = new ReferenceValue();

    private final Set<RecordRef> references;

    public ReferenceValue(RecordRef recordRef) {
        this.references = ImmutableSet.of(recordRef);
    }

    public ReferenceValue(RecordRef... recordRefs) {
        this.references = ImmutableSet.copyOf(recordRefs);
    }

    public ReferenceValue(Iterable<RecordRef> references) {
        this.references = ImmutableSet.copyOf(references);
    }

    public Set<RecordRef> getReferences() {
        return references;
    }

    public RecordRef getOnlyReference() {
        return Iterables.getOnlyElement(references);
    }

    /**
     * If there is only one reference to a record in the given {@code formId}, then
     * return it's record id. Otherwise, return {@code null}
     */
    @Nullable
    public String getOnlyRecordId(ResourceId formId) {
        String key = null;
        for (RecordRef id : references) {
            if(id.getFormId().equals(formId)) {
                // If this is not the first record referenced in the
                // form, then return null
                if(key != null) {
                    return null;
                }
                key = id.getRecordId().asString();
            }
        }
        return key;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return ReferenceType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {

        if(references.size() == 0) {
            return Json.createNull();

        } else if(references.size() == 1) {
            return references.iterator().next().toJsonElement();

        } else {
            JsonValue array = Json.createArray();
            for (RecordRef reference : references) {
                array.add(reference.toJsonElement());
            }
            return array;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceValue that = (ReferenceValue) o;

        return !(references != null ? !references.equals(that.references) : that.references != null);

    }

    @Override
    public int hashCode() {
        return references != null ? references.hashCode() : 0;
    }

    @Override
    public String toString() {
        return references.toString();
    }
}
