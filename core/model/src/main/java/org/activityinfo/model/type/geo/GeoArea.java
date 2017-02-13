package org.activityinfo.model.type.geo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.type.FieldTypeClass;

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
    public JsonElement toJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("blobId", blobId);
        object.add("bbox", envelope.toJsonElement());
        return object;
    }

}
