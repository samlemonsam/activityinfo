package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;

import java.util.List;

public abstract class ExprFunction {

    /**
     * Returns string representation of function (as it is used in expression, e.g. + - ||)
     *
     * @return string representation of function (as it is used in expression)
     */
    public abstract String getId();

    public abstract String getLabel();

    /**
     * Apply this function to the provided arguments.
     */
    public abstract FieldValue apply(List<FieldValue> arguments);

    @Override
    public String toString() {
        return getId();
    }

    /**
     * @return the FieldTypeClass of the result given the argumentTypes as input
     */
    public abstract FieldType resolveResultType(List<FieldType> argumentTypes);

}
