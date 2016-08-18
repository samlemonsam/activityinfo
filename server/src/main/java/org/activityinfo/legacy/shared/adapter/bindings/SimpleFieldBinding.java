package org.activityinfo.legacy.shared.adapter.bindings;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.Map;


public class SimpleFieldBinding implements FieldBinding<EntityDTO> {
    private final ResourceId fieldId;
    private final String propertyName;

    public SimpleFieldBinding(ResourceId fieldId, String propertyName) {
        this.fieldId = fieldId;
        this.propertyName = propertyName;
    }

    @Override
    public void updateInstanceFromModel(FormInstance instance, EntityDTO model) {
        Object value = model.get(propertyName);
        if (value != null) {
            instance.set(fieldId, value);
        }
    }

    @Override
    public void populateChangeMap(FormInstance instance, Map<String, Object> changeMap) {
        Object value = instance.get(fieldId);
        if(value != null) {
            if (value instanceof org.activityinfo.model.type.time.LocalDate) {
                // I want to phase out Rebar time, but it's still needed due to it's use in rebar sql
                org.activityinfo.model.type.time.LocalDate localDate = (org.activityinfo.model.type.time.LocalDate) value;
                value = new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
            } else if (value instanceof NarrativeValue) {
                value = ((NarrativeValue) value).getText();
            } else if (value instanceof TextValue) {
                value = ((TextValue) value).asString();
            } else if (value instanceof BarcodeValue) {
                value = ((BarcodeValue) value).asString();
            } else if (value instanceof Quantity) {
                value = ((Quantity) value).getValue();
            } else if (value instanceof AttachmentValue) {
                value = Resources.toJsonObject(((AttachmentValue) value).asRecord()).toString();
            } else if (value instanceof ReferenceValue) {
                value = Resources.toJsonObject(((ReferenceValue) value).asRecord()).toString();
            } else {
                throw new UnsupportedOperationException(fieldId + " = " + value.getClass().getSimpleName());
            }
        }
        changeMap.put(propertyName, value);
    }
}
