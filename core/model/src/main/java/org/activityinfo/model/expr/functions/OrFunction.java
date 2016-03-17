package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.ColumnView;

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

    @Override
    public int apply(int a, int b) {
        if(a == ColumnView.TRUE || b == ColumnView.TRUE) {
            return ColumnView.TRUE;
        } else if(a == ColumnView.FALSE && b == ColumnView.FALSE) {
            return ColumnView.FALSE;
        } else {
            return ColumnView.NA;
        }
    }
}
