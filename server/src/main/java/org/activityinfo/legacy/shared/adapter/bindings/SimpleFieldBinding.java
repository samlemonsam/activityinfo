package org.activityinfo.legacy.shared.adapter.bindings;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.Map;

/**
 * Created by alex on 2/22/14.
 */
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
                value = ((org.activityinfo.model.type.time.LocalDate) value).atMidnightInMyTimezone();
            } else if (value instanceof NarrativeValue) {
                value = ((NarrativeValue) value).getText();
            } else if (value instanceof TextValue) {
                value = ((TextValue) value).asString();
            } else if (value instanceof BarcodeValue) {
                value = ((BarcodeValue) value).asString();
            } else if (value instanceof Quantity) {
                value = ((Quantity) value).getValue();
            } else {
                throw new UnsupportedOperationException(fieldId + " = " + value.getClass().getSimpleName());
            }
        }
        changeMap.put(propertyName, value);
    }
}
