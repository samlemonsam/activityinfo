package org.activityinfo.model.expr.functions;

public class OrFunction extends BinaryBooleanOperator {

    public static final ExprFunction INSTANCE = new OrFunction();

    public static final String NAME = "||";

    private OrFunction() {
        super(NAME);
    }

    @Override
    public boolean apply(boolean a, boolean b) {
        return a || b;
    }
}
