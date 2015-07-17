package org.activityinfo.legacy.shared.adapter.bindings;

import org.activityinfo.legacy.shared.model.EntityDTO;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.Map;

import static org.activityinfo.model.legacy.CuidAdapter.getLegacyIdFromCuid;

/**
 * Created by alex on 2/22/14.
 */
public class NestedFieldBinding implements FieldBinding<EntityDTO> {
    private final ResourceId fieldId;
    private final char domain;
    private final String propertyName;

    public NestedFieldBinding(ResourceId fieldId, char domain, String propertyName) {
        this.fieldId = fieldId;
        this.domain = domain;
        this.propertyName = propertyName;
    }

    @Override
    public void updateInstanceFromModel(FormInstance instance, EntityDTO model) {
        Object value = model.get(propertyName);
        if (value instanceof EntityDTO) {
            instance.set(fieldId, CuidAdapter.cuid(domain, ((EntityDTO) value).getId()));
        } else if (value instanceof PartnerDTO) { // todo: why PartnerDTO doesn't implement EntityDTO interface?
            instance.set(fieldId, CuidAdapter.cuid(domain, ((PartnerDTO) value).getId()));
        } else if (value instanceof Integer) { // todo: ready to go id
            instance.set(fieldId, CuidAdapter.cuid(domain, (Integer) value));
        }
    }

    @Override
    public void populateChangeMap(FormInstance instance, Map<String, Object> changeMap) {
        ResourceId resourceId = instance.getInstanceId(fieldId);
        if (resourceId != null) {
            changeMap.put(propertyName + "Id", getLegacyIdFromCuid(resourceId));
        }
    }
}
