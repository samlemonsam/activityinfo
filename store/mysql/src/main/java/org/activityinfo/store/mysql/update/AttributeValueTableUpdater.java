package org.activityinfo.store.mysql.update;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityField;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class AttributeValueTableUpdater {

    private Activity activity;
    private final int siteId;
    private Set<Integer> attributesToClear = new HashSet<>();
    private Set<Integer> attributesToSet = new HashSet<>();

    public AttributeValueTableUpdater(Activity activity, ResourceId siteId) {
        this.activity = activity;
        this.siteId = CuidAdapter.getLegacyIdFromCuid(siteId);
        
    }

    public void update(ResourceId fieldId, FieldValue value) {
        Preconditions.checkArgument(fieldId.getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN);
        int attributeGroupId = CuidAdapter.getLegacyIdFromCuid(fieldId);

        ActivityField field = activity.getAttributeGroupField(attributeGroupId);
        EnumType enumType = (EnumType) field.getFormField().getType();
        for (EnumItem enumItem : enumType.getValues()) {
            attributesToClear.add(CuidAdapter.getLegacyIdFromCuid(enumItem.getId()));
        }

        add(value);
    }

    public void add(FieldValue value) {
        EnumValue enumValue = (EnumValue) value;
        if(enumValue != null) {
            for (ResourceId resourceId : enumValue.getResourceIds()) {
                Preconditions.checkArgument(resourceId.getDomain() == CuidAdapter.ATTRIBUTE_DOMAIN);
                int attributeId = CuidAdapter.getLegacyIdFromCuid(resourceId);
                attributesToSet.add(attributeId);
                attributesToClear.remove(attributeId);
            }
        }
    }

    public void executeUpdates(QueryExecutor executor) {
        if(!attributesToClear.isEmpty()) {
            // Set all the existing attribute values for these attribute groups to false
            executor.update(
                "UPDATE attributevalue SET value = FALSE WHERE siteId = ? " +
                    "AND attributeId " + in(attributesToClear),
                Arrays.asList(siteId));
        }

        // Now set the selected to true
        for (Integer attributeId : attributesToSet) {
            executor.update("REPLACE INTO attributevalue (siteId, attributeId, value) VALUES (?, ?, ?)",
                    Arrays.asList(siteId, attributeId, 1));
        }
    }

    private String in(Set<Integer> idSet) {
        if(idSet.isEmpty()) {
            throw new IllegalStateException();
        }
        if(idSet.size() == 1) {
            return " = " + Iterables.getOnlyElement(idSet);
        } else {
            return " IN (" + Joiner.on(", ").join(idSet) + ")";
        }
    }

}
