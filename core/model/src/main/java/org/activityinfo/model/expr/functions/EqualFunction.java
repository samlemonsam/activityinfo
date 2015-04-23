package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldValue;

public class EqualFunction extends ComparisonOperator {

    public static final EqualFunction INSTANCE = new EqualFunction();

    public EqualFunction() {
        super("==");
    }

    public String getLabel() {
        return "Equal";
    }

    @Override
    protected boolean apply(FieldValue a, FieldValue b) {
        return a.equals(b);
    }
}
