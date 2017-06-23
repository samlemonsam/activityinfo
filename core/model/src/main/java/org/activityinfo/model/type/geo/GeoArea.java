package org.activityinfo.model.type.geo;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;

import static org.activityinfo.json.Json.createObject;

/* A Field Value describing a geographic area on the Earth's surface
* in the WGS84 geographic reference system.
*/
public class GeoArea implements GeoFieldValue {

    private Extents envelope;
    private String blobId;

    public GeoArea(Extents envelope, String blobId) {
        this.envelope = envelope;
        this.blobId = blobId;
    }

    public GeoArea(Extents envelope) {
        this.envelope = envelope;
    }


    public Extents getEnvelope() {
        return envelope;
    }

    public String getBlobId() {
        return blobId;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return GeoAreaType.TYPE_CLASS;
    }


    @Override
    public JsonValue toJsonElement() {
        JsonObject object = createObject();
        object.put("blobId", blobId);
        object.put("bbox", envelope.toJsonElement());
        return object;
    }

}
