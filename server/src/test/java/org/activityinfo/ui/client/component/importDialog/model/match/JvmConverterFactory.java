package org.activityinfo.ui.client.component.importDialog.model.match;

import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldParserFactory;

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
