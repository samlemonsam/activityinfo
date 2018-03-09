/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
