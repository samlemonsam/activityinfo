package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;

public class SerialNumberReader implements StringReader {

    private SerialNumberType type;

    public SerialNumberReader(SerialNumberType type) {
        this.type = type;
    }

    @Override
    public String readString(FieldValue value) {
        if(value instanceof SerialNumber) {
            SerialNumber serialNumber = (SerialNumber) value;
            return type.format(serialNumber);
        }
        return null;
    }
}
