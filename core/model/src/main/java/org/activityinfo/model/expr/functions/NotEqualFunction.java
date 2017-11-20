package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldValue;

public class NotEqualFunction extends ComparisonOperator {

    public static final NotEqualFunction INSTANCE = new NotEqualFunction();

    private NotEqualFunction() {
        super("!=");
    }

    public String getLabel() {
        return "Not equal";
    }

    @Override
    protected boolean apply(FieldValue a, FieldValue b) {
        return !EqualFunction.INSTANCE.apply(a, b);
    }

    @Override
    protected boolean apply(double x, double y) {
        return x != y;
    }
}
