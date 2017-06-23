package org.activityinfo.model.type;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonValue;

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

    @Override
    public FieldTypeClass getTypeClass() {
        return ReferenceType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJsonElement() {

        if(references.size() == 0) {
            return Json.createNull();

        } else if(references.size() == 1) {
            return references.iterator().next().toJsonElement();

        } else {
            JsonArray array = Json.createArray();
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
