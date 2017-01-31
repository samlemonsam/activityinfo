package org.activityinfo.ui.client.component.importDialog.model.match;

import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatter;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatterFactory;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Creates a formatter for a field using the standard Java API
 */
public class JavaTextQuantityFormatterFactory implements QuantityFormatterFactory {
    @Override
    public QuantityFormatter create() {
        final NumberFormat format = NumberFormat.getNumberInstance();
        return new QuantityFormatter() {
            @Override
            public String format(Double value) {
                return format.format(value);
            }

            @Override
            public Double parse(String valueAsString) {
                try {
                    // consider strings with '-' not at the start as invalid
                    // e.g. "2012-12-18" is not 2012.0
                    if (valueAsString.indexOf("-") > 1 || valueAsString.contains("/")) {
                        return null;
                    }
                    return format.parse(valueAsString).doubleValue();
                } catch (ParseException e) {
                    return null;
                }
            }
        };
    }
}
