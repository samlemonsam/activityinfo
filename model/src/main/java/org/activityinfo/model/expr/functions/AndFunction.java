package org.activityinfo.model.expr.functions;

import org.activityinfo.model.query.ColumnView;

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

    @Override
    public int apply(int a, int b) {
        if(a == ColumnView.TRUE && b == ColumnView.TRUE) {
            return ColumnView.TRUE;
        } else if(a == ColumnView.FALSE || b == ColumnView.FALSE) {
            return ColumnView.FALSE;
        } else {
            return ColumnView.NA;
        }
    }
}
