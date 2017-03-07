package org.activityinfo.model.type.geo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.RecordFieldValue;
import org.activityinfo.model.type.number.Quantity;

/**
 * A Field Value containing a geographic point in the WGS84 geographic
 * reference system.
 */
public class GeoPoint implements GeoFieldValue, RecordFieldValue {

    private double latitude;
    private double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return GeoPointType.TYPE_CLASS;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("latitude", latitude);
        object.addProperty("longitude", longitude);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPoint geoPoint = (GeoPoint) o;

        if (Double.compare(geoPoint.latitude, latitude) != 0) return false;
        if (Double.compare(geoPoint.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GeoPoint[" + latitude + "," + longitude + "]";
    }

    @Override
    public Extents getEnvelope() {
        return Extents.fromLatLng(latitude, longitude);
    }

    @Override
    public FieldValue getField(String id) {
        switch (id) {
            case "latitude":
                return new Quantity(latitude, "degrees");
            case "longitude":
                return new Quantity(longitude, "degrees");
        }
        return NullFieldValue.INSTANCE;
    }
}
