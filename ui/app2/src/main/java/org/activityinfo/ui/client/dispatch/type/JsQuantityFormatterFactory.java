package org.activityinfo.ui.client.dispatch.type;

import com.google.gwt.i18n.client.NumberFormat;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatter;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatterFactory;

/**
 * Creates QuantityFormatters using the GWT i18n classes.
 */
public class JsQuantityFormatterFactory implements QuantityFormatterFactory {
    @Override
    public QuantityFormatter create() {
        final NumberFormat format = NumberFormat.getDecimalFormat();
        return new QuantityFormatter() {
            @Override
            public String format(Double value) {
                return format.format(value);
            }

            @Override
            public Double parse(String valueAsString) {
                return format.parse(valueAsString);
            }
        };
    }
}
