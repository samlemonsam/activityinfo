package org.activityinfo.store.query.shared;

import org.activityinfo.model.resource.ResourceId;

import java.util.Objects;

public class FieldComponent {
    private String fieldName;
    private String component;

    public FieldComponent(ResourceId fieldId, String component) {
        this.fieldName = fieldId.asString();
        this.component = component;
    }

    public FieldComponent(String fieldName, String component) {
        this.fieldName = fieldName;
        this.component = component;
    }

    public FieldComponent(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getComponent() {
        return component;
    }

    public boolean hasComponent() {
        return component != null;
    }

    @Override
    public String toString() {
        return getCacheKey();
    }

    private String getCacheKey() {
        if(hasComponent()) {
            return fieldName + "." + component;
        } else {
            return fieldName;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldComponent that = (FieldComponent) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, component);
    }

    public ResourceId getFieldId() {
        return ResourceId.valueOf(fieldName);
    }
}
