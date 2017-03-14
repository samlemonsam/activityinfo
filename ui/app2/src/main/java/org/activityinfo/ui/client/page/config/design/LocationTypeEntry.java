package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;


public class LocationTypeEntry extends BaseModelData implements Comparable<LocationTypeEntry> {

    private boolean reference;
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
        this.reference = true;
        composeLabel(locationType);
    }


    /**
     * Create an entry for a locationType
     * @param locationTypeDTO
     * @param databaseName
     */
    public LocationTypeEntry(LocationTypeDTO locationTypeDTO, String databaseName) {
        set("id", locationTypeDTO.getId());
        this.reference = false;
        this.databaseName = Strings.nullToEmpty(databaseName);
        this.locationTypeName = locationTypeDTO.getName();
        composeLabel(locationTypeDTO);
    }

    private void composeLabel(LocationTypeDTO locationType) {
        StringBuilder label = new StringBuilder();
        if(!databaseName.isEmpty()) {
            label.append(databaseName).append(": ");
        }
        label.append(locationType.getName());
        label.append(" (id: ");
        label.append(locationType.getId());
        label.append(")");
        set("label", label.toString());
    }

    @Override
    public int compareTo(LocationTypeEntry other) {
        // "Reference" location types from our GeoDB come first...
        if(this.reference != other.reference) {
            return this.reference ? -1 : +1;
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
