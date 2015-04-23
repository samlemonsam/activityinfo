package org.activityinfo.model.type.geo;

import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordFieldTypeClass;

/**
 * A value type describing a point within the WGS84 Geographic Reference System.
 */
public class GeoPointType implements FieldType {

    public static final String TYPE_ID = "GEOGRAPHIC_POINT";

    public static final GeoPointType INSTANCE = new GeoPointType();

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
            return GeoPoint.fromRecord(record);
        }
    };

    private GeoPointType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

}
