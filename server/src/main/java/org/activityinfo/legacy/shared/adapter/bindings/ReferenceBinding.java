package org.activityinfo.legacy.shared.adapter.bindings;

import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Map;

/**
 * Created by yuriyz on 5/18/2016.
 */
public class ReferenceBinding implements FieldBinding<SiteDTO> {

    private final ResourceId fieldId;
    private final String propertyName;

    public ReferenceBinding(ResourceId fieldId, String propertyName) {
        this.fieldId = fieldId;
        this.propertyName = propertyName;
    }

    @Override
    public void updateInstanceFromModel(FormInstance instance, SiteDTO model) {
        Object value = model.get(propertyName);
        if (value instanceof String) {
            try {
                instance.set(fieldId, ReferenceValue.fromJson((String) value));
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void populateChangeMap(FormInstance instance, Map<String, Object> changeMap) {
        Object value = instance.get(fieldId);
        if (value != null) {
            if (value instanceof ReferenceValue) {
                value = Resources.toJsonObject(((ReferenceValue) value).asRecord()).toString();
            } else {
                throw new UnsupportedOperationException("Unknown value for referece type: " +
                        fieldId + " = " + value.getClass().getSimpleName());
            }
        }
        changeMap.put(propertyName, value);
    }
}
