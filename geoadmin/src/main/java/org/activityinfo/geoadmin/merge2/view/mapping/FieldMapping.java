package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

/**
 * Maps one or more source fields to the target form.
 *
 */
public interface FieldMapping {

    /**
     * @return the id of the resulting field in the target collection
     */
    ResourceId getTargetFieldId();

    /**
     * Map a source row to a field value to imported into the target field.
     */
    FieldValue mapFieldValue(int sourceIndex);
}
