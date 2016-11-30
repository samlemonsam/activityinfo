package org.activityinfo.core.client.type.converter;

import org.activityinfo.core.client.type.formatter.JsQuantityFormatterFactory;
import org.activityinfo.core.shared.type.converter.FieldParserFactory;

/**
 * Creates a converter for a specific field type
 */
public class JsConverterFactory {

    private static FieldParserFactory INSTANCE;

    public static FieldParserFactory get() {
        if(INSTANCE == null) {
            INSTANCE = new FieldParserFactory(
                    new JsQuantityFormatterFactory(),
                    new JsCoordinateNumberFormatter());
        }
        return INSTANCE;
    }

}
