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
