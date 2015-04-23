package org.activityinfo.model.type;

import org.activityinfo.model.resource.Record;

/**
 * Marker interface for {@code FieldTypeClass}es whose values are stored as
 * records.
 */
public interface RecordFieldTypeClass extends FieldTypeClass {

    /**
     * Creates a new {@code FieldValue} from the given serialization {@code Record}
     */
    FieldValue deserialize(Record record);

}
