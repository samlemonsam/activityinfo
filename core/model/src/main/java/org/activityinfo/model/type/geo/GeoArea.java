package org.activityinfo.model.type.geo;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/**
 * A Field Value describing a geographic area on the Earth's surface
 * in the WGS84 geographic reference system.
 */
public class GeoArea implements FieldValue, IsRecord {

    private double minLat = Double.NaN;
    private double maxLat = Double.NaN;
    private double minLon = Double.NaN;
    private double maxLon = Double.NaN;

    private GeoArea() {
    }

    public GeoArea(double minLat, double maxLat, double minLon, double maxLon) {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return GeoAreaType.TYPE_CLASS;
    }

    @Override
    public Record asRecord() {
        Record record = new Record();
        if (!Double.isNaN(minLat)) {
            record.set("minLat", minLat);
        }
        if (!Double.isNaN(maxLat)) {
            record.set("maxLat", maxLat);
        }
        if (!Double.isNaN(minLon)) {
            record.set("minLon", minLon);
        }
        if (!Double.isNaN(maxLon)) {
            record.set("maxLon", maxLon);
        }
        return record;
    }

    public static GeoArea fromRecord(Record record) {
        GeoArea area = new GeoArea();
        if(record.has("minLat")) {
            area.minLat = record.getDouble("minLat");
        }
        if(record.has("maxLat")) {
            area.maxLat = record.getDouble("maxLat");
        }
        if(record.has("minLon")) {
            area.minLon = record.getDouble("minLon");
        }
        if(record.has("maxLon")) {
            area.maxLon = record.getDouble("maxLon");
        }
        return area;
    }
}
