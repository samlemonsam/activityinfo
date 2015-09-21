package org.activityinfo.model.type.geo;

import com.google.common.base.Strings;
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
        Record record = new Record();
        if(envelope != null) {
            record.set("bbox", envelope.asRecord());
        }
        if(!Strings.isNullOrEmpty(blobId)) {
            record.set("blobId", blobId);
        }
        return record;
    }

    public static FieldValue fromRecord(Record record) {
        Extents bbox = null;
        Record bboxRecord = record.isRecord("bbox");
        if(bbox != null) {
            bbox = Extents.fromRecord(bboxRecord);
        }
        String blobId = record.isString("blobId");
        return new GeoArea(bbox, blobId);
    }
}
