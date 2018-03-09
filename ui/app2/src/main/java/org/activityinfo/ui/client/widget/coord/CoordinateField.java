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

import com.extjs.gxt.ui.client.widget.form.TextField;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.io.match.coord.CoordinateAxis;

/**
 * GXT Field for Geographical coordinates. The type of the field is double, but
 * users can enter coordinates in practically any format, which are converted on
 * the fly.
 */
public class CoordinateField extends TextField<Double> {

    /**
     * Because of the conversion between DMS and degrees decimal, we may loose
     * some precision. This becomes a problem when the coordinate is clamped to
     * the administrative bounds, and the resulting value is *exactly* on the
     * boundary. When rounded, the coordinate can fall on the wrong side of the
     * boundary, resulting in a validation error.
     * <p/>
     * The delta value below should be sufficient to allow for such imprecision.
     */
    public static final double DELTA = 0.00001;

    private CoordinateEditor editor;

    /**
     * @param axis
     */
    public CoordinateField(CoordinateAxis axis) {
        super();
        editor = new CoordinateEditor(axis);
        this.setPropertyEditor(editor);
        this.setValidator(editor);
        this.setValidateOnBlur(true);
    }

    /**
     * Sets the bounds for this field
     *
     * @param name     the name of the bounds to present to users in the event of
     *                 violation, (e.g. "Kapisa Province Boundary"
     * @param minValue minimum allowed value for this field
     * @param maxValue maximum allowed value for this field
     */
    public void setBounds(String name, double minValue, double maxValue) {
        editor.setMinValue(minValue - DELTA);
        editor.setMaxValue(maxValue + DELTA);
        editor.setOutOfBoundsMessage(I18N.MESSAGES.coordOutsideBounds(name));
    }
}
