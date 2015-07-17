package org.activityinfo.model.expr.eval;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

/**
 * Wraps a QuantityType field reader, replacing missing values with zeros to support
 * legacy behavior.
 */
public class MissingToZeroReader<InstanceT> implements FieldReader<InstanceT> {

    private final FieldReader<InstanceT> reader;
    private final QuantityType type;

    public MissingToZeroReader(FieldReader<InstanceT> reader) {
        this.reader = reader;
        this.type = (QuantityType) reader.getType();
    }

    @Override
    public FieldValue readField(InstanceT record) {
        return new Quantity(0, type.getUnits());
    }

    @Override
    public FieldType getType() {
        return type;
    }
}
