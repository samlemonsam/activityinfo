package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;


class LocationTypeEntry extends BaseModelData implements Comparable<LocationTypeEntry> {

    private boolean builtin;
    private String databaseName;
    private String locationTypeName;

    private LocationTypeEntry() {
    }


    /**
     * Create an entry for a LocationType from the global admin reference database
     */
    public LocationTypeEntry(LocationTypeDTO locationType) {
        set("id", locationType.getId());
        set("label", locationType.getName());
        this.locationTypeName = locationType.getName();
        this.databaseName = "";
        this.builtin = true;
    }

    public int getId() {
        return get("id");
    }

    public boolean isPublic() {
        return !builtin && databaseName.isEmpty();
    }

    /**
     * Create an entry for a locationType
     * @param locationTypeDTO
     * @param databaseName
     */
    public LocationTypeEntry(LocationTypeDTO locationTypeDTO, String databaseName) {
        set("id", locationTypeDTO.getId());
        set("label", locationTypeDTO.getName());
        this.builtin = false;
        this.databaseName = Strings.nullToEmpty(databaseName);
        this.locationTypeName = locationTypeDTO.getName();
    }

    public String getHeader() {
        if(builtin) {
            return I18N.CONSTANTS.builtInLocationTypes();
        } else if(databaseName.isEmpty()) {
            return I18N.CONSTANTS.publicLocationTypes();
        } else {
            return databaseName;
        }
    }

    public String getLocationTypeName() {
        return locationTypeName;
    }

    @Override
    public int compareTo(LocationTypeEntry other) {
        // "Reference" location types from our GeoDB come first...
        if(this.builtin != other.builtin) {
            return this.builtin ? -1 : +1;
        }

        // Then sort by database name...
        if(!this.databaseName.equals(other.databaseName)) {
            if(this.databaseName.isEmpty()) {
                return +1;
            } else if(other.databaseName.isEmpty()) {
                return -1;
            } else {
                return this.databaseName.compareTo(databaseName);
            }
        }

        // Otherwise sort on the location type name.
        return this.locationTypeName.compareToIgnoreCase(other.locationTypeName);
    }

}
