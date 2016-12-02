package org.activityinfo.core.server.type.converter;

import org.activityinfo.core.server.formatter.JavaTextQuantityFormatterFactory;
import org.activityinfo.core.shared.type.converter.FieldParserFactory;

/**
 * Provides a converter factory using standard JRE classes
 */
public class JvmConverterFactory {

    private static FieldParserFactory INSTANCE;

    public static FieldParserFactory get() {
        if(INSTANCE == null) {
            INSTANCE = new FieldParserFactory(
                    new JavaTextQuantityFormatterFactory(),
                    new JreNumberFormats());
        }
        return INSTANCE;
    }

}
