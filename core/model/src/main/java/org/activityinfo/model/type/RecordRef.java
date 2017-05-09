package org.activityinfo.model.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;

/**
 * Reference to a Record that includes the id of the Form to which the record belongs and
 * as well as the Record's id.
 */
public class RecordRef {

    public static final char SEPARATOR = ':';

    @Nonnull
    private ResourceId formId;

    @Nonnull
    private ResourceId recordId;

    public RecordRef(ResourceId formId, ResourceId recordId) {
        assert formId != null : "formId is null";
        assert recordId != null : "recordId is null";
        this.formId = formId;
        this.recordId = recordId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public ResourceId getRecordId() {
        return recordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordRef recordRef = (RecordRef) o;

        if (!formId.equals(recordRef.formId)) return false;
        return recordId.equals(recordRef.recordId);

    }

    public String toQualifiedString() {
        return formId.asString() + SEPARATOR + recordId.asString();
    }

    public JsonElement toJsonElement() {
        return new JsonPrimitive(toQualifiedString());
    }

    public static RecordRef fromQualifiedString(@Nonnull  String string) {
        int separatorPos = string.indexOf(SEPARATOR);
        if(separatorPos == -1) {
            throw new IllegalStateException("Malformed record ref: " + string);
        }
        ResourceId formId = ResourceId.valueOf(string.substring(0, separatorPos));
        ResourceId recordId = ResourceId.valueOf(string.substring(separatorPos+1));

        return new RecordRef(formId, recordId);
    }

    @Override
    public int hashCode() {
        int result = formId.hashCode();
        result = 31 * result + recordId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RecordRef<" + formId.asString() + ":" + recordId.asString() + ">";
    }
}
