package org.activityinfo.store.query.impl.join;

import org.activityinfo.model.resource.ResourceId;

/**
 * Uniquely identifies a foreign key
 */
public class ForeignKeyId {

    private String fieldName;
    private ResourceId rightFormId;

    public ForeignKeyId(String fieldName, ResourceId rightFormId) {
        this.fieldName = fieldName;
        this.rightFormId = rightFormId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ResourceId getRightFormId() {
        return rightFormId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKeyId that = (ForeignKeyId) o;

        if (!fieldName.equals(that.fieldName)) return false;
        return rightFormId.equals(that.rightFormId);

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + rightFormId.hashCode();
        return result;
    }

    public ResourceId getFieldId() {
        return ResourceId.valueOf(fieldName);
    }
}
