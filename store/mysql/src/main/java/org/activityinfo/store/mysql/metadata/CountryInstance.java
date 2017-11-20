package org.activityinfo.store.mysql.metadata;

import org.activityinfo.model.type.geo.Extents;

import java.io.Serializable;

public class CountryInstance implements Serializable {

    int countryId;
    int locationTypeId;
    String countryName;
    String iso2;
    Extents bounds;

    public CountryInstance(int countryId, int locationTypeId, String countryName, String iso2, double x1, double x2, double y1, double y2) {
        this.countryId = countryId;
        this.locationTypeId = locationTypeId;
        this.countryName = countryName;
        this.iso2 = iso2;
        this.bounds = Extents.create(x1, y1, x2, y2);
    }

    public int getCountryId() {
        return countryId;
    }

    public int getLocationTypeId() {
        return locationTypeId;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getIso2() {
        return iso2;
    }

    public Extents getBounds() {
        return bounds;
    }

}
