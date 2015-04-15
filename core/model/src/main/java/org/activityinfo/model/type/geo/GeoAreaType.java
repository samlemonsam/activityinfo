package org.activityinfo.model.type.geo;

import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordFieldTypeClass;

/**
 * Created by alex on 15-4-15.
 */
public class GeoAreaType implements FieldType {

    public static final String TYPE_ID = "GEOGRAPHIC_AREA";

    public static final GeoAreaType INSTANCE = new GeoAreaType();

    public static final FieldTypeClass TYPE_CLASS = new RecordFieldTypeClass() {
        @Override
        public String getId() {
            return TYPE_ID;
        }

        @Override
        public FieldType createType() {
            return INSTANCE;
        }

        @Override
        public FieldValue deserialize(Record record) {
            return GeoArea.fromRecord(record);
        }
    };

    private GeoAreaType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }
}
