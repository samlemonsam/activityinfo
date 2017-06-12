package org.activityinfo.ui.client.dispatch.type;

import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;
import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldParserFactory;

/**
 * Creates a converter for a specific field type
 */
public class JsConverterFactory {

    private static FieldParserFactory INSTANCE;

    public static FieldParserFactory get() {
        if(INSTANCE == null) {
            INSTANCE = new FieldParserFactory(
                    new JsQuantityFormatterFactory(),
                    JsCoordinateNumberFormatter.INSTANCE);
        }
        return INSTANCE;
    }

}
