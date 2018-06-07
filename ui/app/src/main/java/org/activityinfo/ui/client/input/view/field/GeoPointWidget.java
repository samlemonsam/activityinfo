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
package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.logging.Logger;


/**
 * FieldWidget for {@link org.activityinfo.model.type.geo.GeoPointType} fields.
 */
public class GeoPointWidget implements FieldWidget {

    private static final Logger LOGGER = Logger.getLogger(GeoPointWidget.class.getName());

    private static final int DEBOUNCE_DELAY_MS = 100;

    private Container panel;
    private CoordField latitude;
    private CoordField longitude;
    private FieldUpdater updater;
    private final Timer timer;


    public GeoPointWidget(FieldUpdater updater) {
        this.updater = updater;
        latitude = new CoordField(CoordinateAxis.LATITUDE);
        longitude = new CoordField(CoordinateAxis.LONGITUDE);

        panel = new FlowLayoutContainer();
        panel.add(latitude.fieldLabel);
        panel.add(longitude.fieldLabel);

        latitude.field.addKeyUpHandler(this::onKeyUp);
        longitude.field.addKeyUpHandler(this::onKeyUp);

        latitude.field.addBlurHandler(event -> updater.touch());
        longitude.field.addBlurHandler(event -> updater.touch());

        timer = new Timer() {
            @Override
            public void run() {
                updateModel();
            }
        };
    }


    private void onKeyUp(KeyUpEvent event) {

        if(timer.isRunning()) {
            timer.cancel();
        }
        timer.schedule(DEBOUNCE_DELAY_MS);
    }

    private void updateModel() {
        latitude.parseAndValidate();
        longitude.parseAndValidate();


        if(latitude.empty && longitude.empty) {
            // If both coordinates are empty, consider this to
            // be an empty field and clear the field validation messages
            // (the form may still mark this field as a whole if the field is
            //  required, but we don't have to deal with that here)
            latitude.field.clearInvalid();
            longitude.field.clearInvalid();
            updater.update(FieldInput.EMPTY);

        } else {

            // If there is *any* input in *either* field, then
            // either the field is VALID, or it's INVALID.

            if (latitude.valid && longitude.valid) {
                updater.update(new FieldInput(new GeoPoint(latitude.value, longitude.value)));

            } else {
                latitude.markInvalidIfEmpty();
                longitude.markInvalidIfEmpty();
                updater.update(FieldInput.INVALID_INPUT);
            }
        }
    }

    @Override
    public void init(FieldValue value) {
        if(value instanceof GeoPoint) {
            GeoPoint pointValue = (GeoPoint) value;
            latitude.init(pointValue.getLatitude());
            longitude.init(pointValue.getLongitude());
        }
    }

    @Override
    public void clear() {
        latitude.clear();
        longitude.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        latitude.field.setEnabled(relevant);
        longitude.field.setEnabled(relevant);
    }

    @Override
    public void focus() {
        latitude.field.focus();
    }

    @Override
    public Widget asWidget() {
        return panel;
    }


    private static class CoordField {
        private final CoordinateAxis axis;
        private final CoordinateParser parser;
        private final TextField field;
        private final FieldLabel fieldLabel;

        private Double value;
        private boolean empty = true;
        private boolean valid;

        public CoordField(CoordinateAxis axis) {
            this.axis = axis;
            this.parser = new CoordinateParser(axis, JsCoordinateNumberFormatter.INSTANCE);
            this.field = new TextField();
            this.field.setValidateOnBlur(false);
            this.field.setAutoValidate(false);
            this.fieldLabel = new FieldLabel(field, axis.getLocalizedName());
        }

        public void init(double coordinateValue) {
            value = coordinateValue;
            valid = true;
            empty = false;
            field.setText(parser.format(coordinateValue));
            field.clearInvalid();
        }

        public void clear() {
            value = null;
            empty = true;
            field.clear();
            field.clearInvalid();
        }

        /**
         * Tries to parse the text into a valid coordinate, and updates our {@code coordinateValue}
         */
        private void parseAndValidate() {

            String textInput = field.getText();

            empty = Strings.isNullOrEmpty(textInput);
            if(empty) {
                value = null;
                valid = false;
            } else {
                try {
                    value = parser.parse(field.getText());
                    valid = true;

                    field.clearInvalid();

                } catch (CoordinateFormatException e) {
                    field.markInvalid(e.getMessage());
                    valid = false;
                }
            }
        }

        private void markInvalidIfEmpty() {
            if(empty) {
                field.markInvalid(I18N.CONSTANTS.noNumber());
            }
        }
    }
}
