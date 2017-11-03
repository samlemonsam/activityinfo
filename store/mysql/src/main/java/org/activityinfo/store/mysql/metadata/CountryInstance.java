package org.activityinfo.store.mysql.metadata;

import java.io.Serializable;

public class CountryInstance implements Serializable {

    int countryId;
    int locationTypeId;
    String countryName;
    String iso2;
    double x1;
    double x2;
    double y1;
    double y2;

    public CountryInstance(int countryId, int locationTypeId, String countryName, String iso2, double x1, double x2, double y1, double y2) {
        this.countryId = countryId;
        this.locationTypeId = locationTypeId;
        this.countryName = countryName;
        this.iso2 = iso2;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
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

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY1() {
        return y1;
    }

    public double getY2() {
        return y2;
    }

}
