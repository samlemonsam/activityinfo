package org.activityinfo.model.type.geo;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/* A Field Value describing a geographic area on the Earth's surface
* in the WGS84 geographic reference system.
*/
public class GeoArea implements GeoFieldValue, IsRecord {

    private Extents envelope;
    private String blobId;

    public GeoArea(Extents envelope, String blobId) {
        this.envelope = envelope;
        this.blobId = blobId;
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
    public Record asRecord() {
        throw new UnsupportedOperationException("todo");
    }

    public static FieldValue fromRecord(Record record) {
        throw new UnsupportedOperationException("todo");
    }
}
