package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;

class LocationTypeFieldBinding extends FieldBinding {

    private CountryDTO country;

    public LocationTypeFieldBinding(final CountryDTO country, final ComboBox<LocationTypeEntry> field, String property) {

        super(field, property);
        this.country = country;
        setConverter(new Converter() {
            @Override
            public LocationTypeEntry convertModelValue(Object value) {
                Integer locationTypeId = (Integer) value;
                if(locationTypeId == null) {
                    return null;
                }
                LocationTypeDTO locationType = country.getLocationTypeById(locationTypeId);
                if(locationType == null) {
                    return null;
                }
                return new LocationTypeEntry(locationType);
            }

            @Override
            public Integer convertFieldValue(Object value) {
                return ((ModelData) value).get("id");
            }
        });
    }
}
