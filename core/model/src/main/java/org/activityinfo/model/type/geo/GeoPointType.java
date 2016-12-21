package org.activityinfo.model.type.geo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.number.QuantityType;

/**
 * A value type describing a point within the WGS84 Geographic Reference System.
 */
public class GeoPointType implements RecordFieldType {

    public static final String TYPE_ID = "geopoint";

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

    };

    private GeoPointType() {  }

    @Override
    public FieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonElement value) {
        JsonObject object = (JsonObject) value;
        GeoPoint point = new GeoPoint(
                object.get("latitude").getAsDouble(),
                object.get("longitude").getAsDouble());
        
        return point;
    }

    /**
     *
     * @return the singleton instance for this type
     */
    private Object readResolve() {
        return INSTANCE;
    }


    @Override
    public FormClass getFormClass() {
        FormClass formClass = new FormClass(ResourceId.valueOf("geoPoint"));
        formClass.setLabel(I18N.CONSTANTS.geographicCoordinatesFieldLabel());
        formClass.addField(ResourceId.valueOf("latitude"))
                .setCode("latitude")
                .setLabel(I18N.CONSTANTS.latitude())
                .setType(new QuantityType("degrees"))
                .setRequired(true);
        formClass.addField(ResourceId.valueOf("longitude"))
                .setCode("longitude")
                .setLabel(I18N.CONSTANTS.longitude())
                .setType(new QuantityType("degrees"))
                .setRequired(true);
        
        return formClass;
    }
}
