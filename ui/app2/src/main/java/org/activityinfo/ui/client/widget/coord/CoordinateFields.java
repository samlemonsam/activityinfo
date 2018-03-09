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
package org.activityinfo.ui.client.widget.coord;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.model.type.geo.Extents;

public class CoordinateFields {
    private CoordinateField latitudeField;
    private CoordinateField longitudeField;

    public CoordinateFields() {
        longitudeField = new CoordinateField(CoordinateAxis.LONGITUDE);
        longitudeField.setFieldLabel(I18N.CONSTANTS.longitude());

        latitudeField = new CoordinateField(CoordinateAxis.LATITUDE);
        latitudeField.setFieldLabel(I18N.CONSTANTS.latitude());
    }

    public void setToolTip(String toolTip) {
        longitudeField.setToolTip(toolTip);
        latitudeField.setToolTip(toolTip);
    }

    public void setBounds(String name, Extents bounds) {
        longitudeField.setBounds(name, bounds.getMinLon(), bounds.getMaxLon());
        latitudeField.setBounds(name, bounds.getMinLat(), bounds.getMaxLat());
    }

    public CoordinateField getLatitudeField() {
        return latitudeField;
    }

    public CoordinateField getLongitudeField() {
        return longitudeField;
    }

    public void setValue(AiLatLng latLng) {
        if (latLng == null) {
            latitudeField.setValue(null);
            longitudeField.setValue(null);
        } else {
            latitudeField.setValue(latLng.getLat());
            longitudeField.setValue(latLng.getLng());
        }
    }

    public AiLatLng getValue() {
        if (latitudeField.getValue() == null
                || longitudeField.getValue() == null) {
            return null;
        } else {
            return new AiLatLng(latitudeField.getValue(),
                    longitudeField.getValue());
        }
    }

    public void addChangeListener(final Listener<FieldEvent> listener) {
        Listener<FieldEvent> fieldListener = new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                listener.handleEvent(be);
            }
        };
        latitudeField.addListener(Events.Change, fieldListener);
        longitudeField.addListener(Events.Change, fieldListener);
    }

    public boolean validate() {
        return latitudeField.validate() && longitudeField.validate();
    }

    public void setVisible(boolean visible) {
        latitudeField.setVisible(visible);
        longitudeField.setVisible(visible);
    }

    public void setReadOnly(boolean readOnly) {
        latitudeField.setReadOnly(readOnly);
        longitudeField.setReadOnly(readOnly);
    }

}
