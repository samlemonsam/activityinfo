package org.activityinfo.ui.client.component.importDialog.model.type.converter;

import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.component.importDialog.model.type.formatter.QuantityFormatter;

import javax.annotation.Nonnull;

/**
 * Converts string values to a quantity
 */
public class QuantityParser implements FieldValueParser {

    private final QuantityType type;
    private final QuantityFormatter formatter;

    public QuantityParser(QuantityType type, QuantityFormatter formatter) {
        this.type = type;
        this.formatter = formatter;
    }

    @Nonnull
    @Override
    public Quantity convert(@Nonnull String value) {
        return new Quantity(formatter.parse(value), type.getUnits());
    }
}
