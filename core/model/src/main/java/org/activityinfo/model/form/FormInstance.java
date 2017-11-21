package org.activityinfo.model.form;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.activityinfo.json.Json.createObject;

/**
 *
 * Wrapper for a {@code Record} or {@code Resource} that exposes its properties
 * as {@code FieldValue}s
 *
 * @author yuriyz on 1/29/14.
 */
public class FormInstance {

    private ResourceId id;
    private ResourceId classId;
    private ResourceId parentRecordId;
    private Map<ResourceId, FieldValue> fieldMap;

    /**
     * Constructs a new FormInstance. To obtain an id for a new instance
     * use
     *
     * @param id the id of the instance.
     * @param classId the id of this form's class
     */
    public FormInstance(@Nonnull ResourceId id, @Nonnull ResourceId classId) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(classId);
        this.id = id;
        this.classId = classId;
        this.parentRecordId = classId;
        this.fieldMap = new HashMap<>();
    }

    public FormInstance(RecordRef recordRef) {
        this(recordRef.getRecordId(), recordRef.getFormId());
    }

    public static FormInstance toFormInstance(FormClass formClass, FormRecord record) {
        FormInstance instance = new FormInstance(ResourceId.valueOf(record.getRecordId()), formClass.getId());

        if (record.getParentRecordId() != null) {
            instance.setParentRecordId(ResourceId.valueOf(record.getParentRecordId()));
        }

        for (FormField field : formClass.getFields()) {
            JsonValue fieldValue = record.getFields().get(field.getName());
            if(fieldValue != null && !fieldValue.isJsonNull()) {
                instance.set(field.getId(), field.getType().parseJsonValue(fieldValue));
            }
        }
        return instance;
    }

    public ResourceId getId() {
        return id;
    }

    public RecordRef getRef() {
        return new RecordRef(getFormId(), getId());
    }

    public FormInstance setId(ResourceId id) {
        this.id = id;
        return this;
    }

    public FormInstance setClassId(ResourceId classId) {
        this.classId = classId;
        return this;
    }


    public ResourceId getFormId() {
        return classId;
    }

    public FormInstance setParentRecordId(ResourceId parentRecordId) {
        assert parentRecordId != null;
        this.parentRecordId = parentRecordId;
        return this;
    }

    public ResourceId getParentRecordId() {
        return parentRecordId;
    }

    public Map<ResourceId, FieldValue> getFieldValueMap() {
        return new HashMap<>(fieldMap);
    }

    public void removeAll(Set<ResourceId> fieldIds) {
        for (ResourceId fieldId : fieldIds) {
            fieldMap.remove(fieldId);
        }
    }

    public void setAll(Map<ResourceId, FieldValue> valueMap) {
        fieldMap.putAll(valueMap);
    }

    public FormInstance set(@Nonnull ResourceId fieldId, String value) {
        if(value == null) {
            fieldMap.remove(fieldId);
        } else {
            fieldMap.put(fieldId, TextValue.valueOf(value));
        }
        return this;
    }


    public FormInstance set(String fieldId, String name) {
        return set(ResourceId.valueOf(fieldId), name);
    }

    public FormInstance set(@Nonnull ResourceId fieldId, double value) {
        return set(fieldId, new Quantity(value));
    }

    public FormInstance set(@Nonnull ResourceId fieldId, boolean value) {
        fieldMap.put(fieldId, BooleanFieldValue.valueOf(value));
        return this;
    }

    public FormInstance set(@Nonnull ResourceId fieldId, FieldValue fieldValue) {
        fieldMap.put(fieldId, fieldValue);
        return this;
    }

    public FormInstance set(@Nonnull ResourceId fieldId, AiLatLng latLng) {
        fieldMap.put(fieldId, new GeoPoint(latLng.getLat(), latLng.getLng()));
        return this;
    }

    public FieldValue get(ResourceId fieldId, FieldType fieldType) {
        FieldValue value = fieldMap.get(fieldId);
        if(value != null && value.getTypeClass() == fieldType.getTypeClass()) {
            return value;
        } else {
            return null;
        }
    }

    public FieldValue get(ResourceId fieldId) {
        return fieldMap.get(fieldId);
    }


    /**
     * Returns the value of {@code fieldId} if the value is present and of
     * the specified {@code typeClass}, or {@code null} otherwise.
     */
    public FieldValue get(ResourceId fieldId, FieldTypeClass typeClass) {
        FieldValue value = get(fieldId);
        if(value != null && value.getTypeClass() == typeClass) {
            return value;
        } else {
            return null;
        }
    }

    public String getString(ResourceId fieldId) {
        FieldValue value = fieldMap.get(fieldId);
        if(value instanceof HasStringValue) {
            return ((HasStringValue) value).asString();
        }
        return null;
    }

    public LocalDate getDate(ResourceId fieldId) {
        FieldValue value = get(fieldId);
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }


    public Double getDouble(ResourceId fieldId) {
        FieldValue value = get(fieldId);
        if(value instanceof Quantity) {
            return ((Quantity) value).getValue();
        }
        return null;
    }

    public FormInstance copy() {
        final FormInstance copy = new FormInstance(getId(), getFormId());
        copy.fieldMap.putAll(fieldMap);
        return copy;
    }

    public AiLatLng getPoint(ResourceId fieldId) {
        FieldValue value = get(fieldId);
        if(value instanceof GeoPoint) {
            GeoPoint geoPoint = (GeoPoint) value;
            return new AiLatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        }
        return null;
    }

    public int size() {
        return fieldMap.size();
    }

    @Override
    public String toString() {
        return "FormInstance{" +
                "id=" + id +
                ", classId=" + classId +
                ", parentRecordId=" + parentRecordId +
                ", fieldMap=" + fieldMap +
                '}';
    }

    public boolean isEmpty() {
        return fieldMap.isEmpty();
    }

    public boolean isEmpty(String... fieldsToIgnore) {
        Map<ResourceId, FieldValue> copy = new HashMap<>(fieldMap);
        for (String field : fieldsToIgnore) {
            copy.remove(ResourceId.valueOf(field));
        }
        return copy.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormInstance instance = (FormInstance) o;

        return !(id != null ? !id.equals(instance.id) : instance.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public JsonValue toJsonObject() {

        JsonValue fields = createObject();
        for (Map.Entry<ResourceId, FieldValue> entry : fieldMap.entrySet()) {
            if(entry.getValue() != null) {
                fields.put(entry.getKey().asString(), entry.getValue().toJsonElement());
            }
        }

        JsonValue object = createObject();
        object.put("formId", getFormId().asString());
        object.put("recordId", getId().asString());
        object.put("fieldValues", fields);

        return object;
    }

}
