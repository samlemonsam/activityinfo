package org.activityinfo.model.type.number;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

public class Quantity implements FieldValue {

    private final double value;

    public Quantity(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return FieldTypeClass.QUANTITY;
    }

    @Override
    public JsonValue toJsonElement() {
        if(Double.isNaN(value)) {
            return Json.createNull();
        } else {
            return Json.create(value);
        }
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
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return ((int) (temp ^ (temp >>> 32)));
    }

    @Override
    public String toString() {
        return "Quantity{" +
               "value=" + value +
               '}';
    }
}
