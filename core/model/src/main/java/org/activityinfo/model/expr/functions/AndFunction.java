package org.activityinfo.model.expr.functions;

public class AndFunction extends BinaryBooleanOperator {

    public static final AndFunction INSTANCE = new AndFunction();

    public static final String NAME = "&&";

    private AndFunction() {
        super(NAME);
    }

    @Override
    public boolean apply(boolean a, boolean b) {
        return a && b;
    }
}
