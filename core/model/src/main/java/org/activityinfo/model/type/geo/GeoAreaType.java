package org.activityinfo.model.type.geo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.type.*;

/**
 * A value type describing a geographic area on the Earth's surface
 * in the WGS84 geographic reference system.
 */
public class GeoAreaType implements FieldType {

    public static final String TYPE_ID = "geoArea";

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

    };

    private GeoAreaType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        JsonObject object = value.getAsJsonObject();
        JsonObject bbox = object.getAsJsonObject("bbox");
        return new GeoArea(Extents.fromJsonObject(bbox));
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitGeoArea(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    /**
     * 
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }
            
}
