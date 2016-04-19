package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.AdminLevelResult;

/**
 * Created by yuriyz on 4/19/2016.
 */
public class GetAdminLevelsByCountry implements Command<AdminLevelResult> {

    private int countryId;

    public GetAdminLevelsByCountry() {
    }

    public GetAdminLevelsByCountry(int countryId) {
        this.countryId = countryId;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }
}