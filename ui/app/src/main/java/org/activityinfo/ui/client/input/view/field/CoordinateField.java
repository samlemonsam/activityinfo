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

import com.sencha.gxt.cell.core.client.form.NumberInputCell;
import com.sencha.gxt.cell.core.client.form.ValueBaseInputCell;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;

import java.text.ParseException;

public class CoordinateField extends ValueBaseField<Double> {

    public CoordinateField(CoordinateAxis axis) {
        this(axis, new CoordinateParser(axis, JsCoordinateNumberFormatter.INSTANCE));
    }

    private CoordinateField(CoordinateAxis axis, CoordinateParser parser) {
        super(inputCell(axis, parser));
    }

    private static ValueBaseInputCell<Double> inputCell(CoordinateAxis axis, CoordinateParser parser) {

        NumberPropertyEditor<Double> propertyEditor = new NumberPropertyEditor.DoublePropertyEditor() {
            @Override
            public Double parse(CharSequence text) throws ParseException {
                try {
                    return parser.parse(text.toString());
                } catch (CoordinateFormatException e) {
                    throw new ParseException(e.getMessage(), 0);
                }
            }

            @Override
            public String render(Double object) {
                return parser.format(object);
            }
        };

        NumberInputCell<Double> numberInputCell = new NumberInputCell<Double>(propertyEditor);
        numberInputCell.setBaseChars("0123456789" +
                axis.getNegativeHemisphereCharacters() +
                axis.getPositiveHemisphereCharacters() +
                ".,'\"°");
        numberInputCell.setAllowDecimals(true);
        numberInputCell.setAllowNegative(true);
        numberInputCell.setHideTrigger(true);
        numberInputCell.setClearValueOnParseError(false);

        return numberInputCell;
    }
}
