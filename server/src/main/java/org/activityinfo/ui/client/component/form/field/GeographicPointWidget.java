package org.activityinfo.ui.client.component.form.field;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.type.converter.CoordinateAxis;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.promise.Promise;

/**
 * @author yuriyz on 1/31/14.
 */
public class GeographicPointWidget implements FormFieldWidget<GeoPoint> {

    private final FlowPanel panel;
    private final ValueUpdater<GeoPoint> valueUpdater;

    private CoordinateBox latitudeBox;
    private CoordinateBox longitudeBox;

    public GeographicPointWidget(final ValueUpdater<GeoPoint> valueUpdater) {
        this.valueUpdater = valueUpdater;

        latitudeBox = new CoordinateBox(CoordinateAxis.LATITUDE);
        longitudeBox = new CoordinateBox(CoordinateAxis.LONGITUDE);

        panel = new FlowPanel();
        panel.add(latitudeBox);
        panel.add(longitudeBox);

        ValueChangeHandler<Double> changeHandler = new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                fireValueChanged();
            }
        };

        latitudeBox.addValueChangeHandler(changeHandler);
        longitudeBox.addValueChangeHandler(changeHandler);
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    protected GeoPoint getValue() {
        Double latitude = latitudeBox.getValue();
        Double longitude = longitudeBox.getValue();

        if(latitude != null && longitude != null) {
            return new GeoPoint(latitude, longitude);
        } else {
            return null;
        }
    }


    @Override
    public void setReadOnly(boolean readOnly) {
        latitudeBox.setReadOnly(readOnly);
        longitudeBox.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return latitudeBox.isReadOnly();
    }

    @Override
    public Promise<Void> setValue(GeoPoint value) {
        longitudeBox.setValue(value.getLongitude());
        latitudeBox.setValue(value.getLongitude());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        latitudeBox.setValue(null);
        longitudeBox.setValue(null);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public boolean isValid() {
        // If either field has invalid text, it's invalid
        if(!latitudeBox.isValid() || !longitudeBox.isValid()) {
            return false;
        }

        // Either both fields are empty, or both fields are filled.
        return (latitudeBox.getValue() == null && longitudeBox.getValue() == null) ||
               (latitudeBox.getValue() != null && longitudeBox.getValue() != null);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
