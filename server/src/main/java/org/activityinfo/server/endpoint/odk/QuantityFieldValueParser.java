package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Strings;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

class QuantityFieldValueParser implements FieldValueParser {
    final private String units;

    QuantityFieldValueParser(QuantityType quantityType) {
        this.units = quantityType.getUnits();
    }

    @Override
    public FieldValue parse(String text) {

        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        double value;
        try {
            value = Double.parseDouble(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quantity field value: " + text, e);
        }

        return new Quantity(value);
    }
}
