package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.ValueBaseInputCell;
import com.sencha.gxt.theme.base.client.field.TextFieldDefaultAppearance;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;

import java.text.ParseException;

public class CoordinateField extends ValueBaseField<Double> {

    public CoordinateField(CoordinateAxis axis) {
        this(new CoordinateParser(axis, JsCoordinateNumberFormatter.INSTANCE));
    }

    private CoordinateField(CoordinateParser parser) {
        super(new ValueBaseInputCell<Double>(new TextFieldDefaultAppearance()) {
            @Override
            public void render(Context context, Double value, SafeHtmlBuilder sb) {
                sb.appendEscaped(parser.format(value));
            }
        }, new PropertyEditor<Double>() {
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
        });

    }

}
