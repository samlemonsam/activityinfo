package org.activityinfo.model.formula.functions;

import org.activityinfo.model.formula.diagnostic.InvalidTypeException;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.HasSetFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.HasStringValue;

import java.util.Set;

public class Casting {
    public static Quantity toQuantity(FieldValue fieldValue) {
        if(fieldValue instanceof Quantity) {
            return (Quantity) fieldValue;
        } else {
            return new Quantity(Double.NaN);
        }
    }

    public static boolean toBoolean(FieldValue value) {
        if(value == BooleanFieldValue.TRUE) {
            return true;
        } else if(value == BooleanFieldValue.FALSE) {
            return false;
        } else {
            throw new InvalidTypeException("Cannot cast [" + value + "] to boolean");
        }
    }

    public static String toString(FieldValue value) {
        if(value instanceof HasStringValue) {
            return ((HasStringValue) value).asString();
        }
        throw new InvalidTypeException("Cannot cast field value of type " + value.getTypeClass() + " to string");
    }

    public static Set<ResourceId> toSet(FieldValue value) {
        if (value instanceof HasSetFieldValue) {
            return ((HasSetFieldValue) value).getResourceIds();
        }else {
            throw new InvalidTypeException("Cannot cast [" + value + "] to Set<ResourceId>");
        }
    }

}
