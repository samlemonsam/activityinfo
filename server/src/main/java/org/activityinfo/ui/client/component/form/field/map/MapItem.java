package org.activityinfo.ui.client.component.form.field.map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.ui.client.component.form.field.OptionSet;
import org.discotools.gwt.leaflet.client.types.LatLng;

import java.util.Collection;
import java.util.Set;

/**
 * Created by yuriyz on 9/14/2016.
 */
public class MapItem {

    private RecordRef ref;
    private String label;
    private double latitude;
    private double longitude;

    public MapItem(RecordRef ref, String label, double latitude, double longitude) {
        Preconditions.checkNotNull(ref);

        this.ref = ref;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Set<MapItem> items(ResourceId formId, OptionSet optionSet, String geoPointColumnName) {
        Set<MapItem> items = Sets.newHashSet();
        for (int i = 0; i < optionSet.getCount(); i++) {
            Extents extents = optionSet.getColumnView(geoPointColumnName).getExtents(i);
            items.add(new MapItem(new RecordRef(formId, ResourceId.valueOf(optionSet.getId(i))),
                    optionSet.getLabel(i),
                    extents.getX1(),
                    extents.getY1()));
        }
        return items;
    }

    public static Optional<MapItem> byId(Collection<MapItem> items, String id) {
        for (MapItem item : items) {
            if (item.getId().equals(id)) {
                return Optional.of(item);
            }
        }
        return Optional.absent();
    }

    public RecordRef getId() {
        return ref;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean hasLatLng() {
        return latitude != Double.NaN && longitude != Double.NaN;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapItem mapItem = (MapItem) o;

        if (Double.compare(mapItem.latitude, latitude) != 0) return false;
        if (Double.compare(mapItem.longitude, longitude) != 0) return false;
        if (!ref.equals(mapItem.ref)) return false;
        return label != null ? label.equals(mapItem.label) : mapItem.label == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = ref.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MapItem{" +
                "ref='" + ref + '\'' +
                ", label='" + label + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
