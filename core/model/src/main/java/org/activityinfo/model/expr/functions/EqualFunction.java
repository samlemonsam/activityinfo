package org.activityinfo.model.expr.functions;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextValue;

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

        if(a instanceof TextValue && b instanceof ReferenceValue) {
            return compare(((ReferenceValue) b), ((TextValue) a));
        }

        if(a instanceof ReferenceValue && b instanceof TextValue) {
            return compare(((ReferenceValue) a), ((TextValue) b));
        }

        return a.equals(b);
    }

    private boolean compare(ReferenceValue refValue, TextValue textValue) {
        if(refValue.getReferences().size() == 1) {
            RecordRef recordRef = refValue.getReferences().iterator().next();
            return recordRef.getRecordId().asString().equals(textValue.asString());
        }
        return false;
    }

    @Override
    protected boolean apply(double x, double y) {
        return x == y;
    }
}
