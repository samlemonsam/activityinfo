package org.activityinfo.model.type.number;

import com.google.common.base.Strings;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class Quantity implements FieldValue, IsRecord {

    public static final String UNKNOWN_UNITS = "unknown";

    private final double value;
    private String units;

    public Quantity(double value) {
        this(value, UNKNOWN_UNITS);
    }

    public Quantity(double value, String units) {
        this.value = value;
        this.units = Strings.emptyToNull(units);
    }

    public double getValue() {
        return value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return FieldTypeClass.QUANTITY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quantity quantity = (Quantity) o;

        if (Double.compare(quantity.value, value) != 0) {
            return false;
        }

        if (units == null) {
            return quantity.units == null;
        } else {
            return units.equals(quantity.units);
        }
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return ((int) (temp ^ (temp >>> 32))) ^ (units != null ? units.hashCode() : 0);
    }

    public static FieldValue fromRecord(Record record) {
        return new Quantity(record.getDouble("value"), record.isString("units"));
    }

    @Override
    public Record asRecord() {
        return new Record()
            .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
            .set("value", value)
            .set("units", units);
    }

    public boolean hasUnits() {
        return units != null;
    }

    @Override
    public String toString() {
        return "Quantity{" +
               "value=" + value +
               ", units='" + units + '\'' +
               '}';
    }
}
