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
