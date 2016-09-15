package org.activityinfo.ui.client.component.form.field.map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.ui.client.component.form.field.OptionSet;

import java.util.Collection;
import java.util.Set;

/**
 * Created by yuriyz on 9/14/2016.
 */
public class MapItem {

    private String id;
    private String label;
    private double latitude;
    private double longitude;

    public MapItem(String id, String label, double latitude, double longitude) {
        Preconditions.checkNotNull(id);

        this.id = id;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Set<MapItem> items(OptionSet optionSet, String geoPointColumnName) {
        Set<MapItem> items = Sets.newHashSet();
        for (int i = 0; i < optionSet.getCount(); i++) {
            Extents extents = optionSet.getColumnView(geoPointColumnName).getExtents(i);
            items.add(new MapItem(optionSet.getId(i), optionSet.getLabel(i), extents.getX1(), extents.getY1()));
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapItem item = (MapItem) o;

        return !(id != null ? !id.equals(item.id) : item.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MapItem{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
