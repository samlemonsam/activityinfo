package org.activityinfo.store.mysql.update;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.store.mysql.collections.Activity;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class AttributeValueTableUpdater {

    private final int siteId;
    private Set<Integer> attributeGroupsToClear = new HashSet<>();
    private Set<Integer> attributesToSet = new HashSet<>();

    public AttributeValueTableUpdater(Activity activity, ResourceId siteId) {
        this.siteId = CuidAdapter.getLegacyIdFromCuid(siteId);
        
    }

    public void update(ResourceId fieldId, FieldValue value) {
        Preconditions.checkArgument(fieldId.getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN);
        int attributeGroupId = CuidAdapter.getLegacyIdFromCuid(fieldId);
        
        attributeGroupsToClear.add(attributeGroupId);

        EnumValue enumValue = (EnumValue) value;
        for (ResourceId resourceId : enumValue.getResourceIds()) {
            Preconditions.checkArgument(resourceId.getDomain() == CuidAdapter.ATTRIBUTE_DOMAIN);
            int attributeId = CuidAdapter.getLegacyIdFromCuid(resourceId);
            attributesToSet.add(attributeId);
        }
    }
    
    public void executeUpdates(QueryExecutor executor) {
        if(!attributeGroupsToClear.isEmpty()) {
            // Set all the existing attribute values for these attribute groups to false
            executor.update(
                "UPDATE attributevalue SET value = FALSE WHERE siteId = ? " +
                    "AND attributeId in " +
                        " (SELECT attributeId FROM attribute WHERE attributeGroupId " + in(attributeGroupsToClear) + ")",
                    Arrays.asList(siteId));

            // Now set the selected to true
            for (Integer attributeId : attributesToSet) {
                executor.update("REPLACE INTO attributevalue (siteId, attributeId, value) VALUES (?, ?, ?)",
                        Arrays.asList(siteId, attributeId, 1));
            }
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
