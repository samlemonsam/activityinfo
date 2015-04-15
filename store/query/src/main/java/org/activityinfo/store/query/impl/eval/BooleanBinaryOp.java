package org.activityinfo.store.query.impl.eval;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;

class BooleanBinaryOp implements ColumnView {

    public enum Operator {
        AND {
            @Override
            public int apply(int x, int y) {
                if(x == NA || y == NA) {
                    return NA;
                } else if(x == TRUE && y == TRUE) {
                    return TRUE;
                } else {
                    return FALSE;
                }
            }
        }, OR {
            @Override
            public int apply(int x, int y) {
                if(x == TRUE || y == TRUE) {
                    return TRUE;
                } else if(x == NA || y == NA) {
                    return NA;
                } else {
                    return FALSE;
                }
            }
        };

        public abstract int apply(int x, int y);
    }

    private ColumnView x;
    private ColumnView y;
    private Operator operator;

    public BooleanBinaryOp(Operator operator, ColumnView x, ColumnView y) {
        this.x = x;
        this.y = y;
        this.operator = operator;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return x.numRows();
    }

    @Override
    public Object get(int row) {
        return getBoolean(row);
    }

    @Override
    public double getDouble(int row) {
        return 0;
    }

    @Override
    public String getString(int row) {
        return null;
    }

    @Override
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        int bx = x.getBoolean(row);
        int by = y.getBoolean(row);
        return operator.apply(bx, by);
    }
}
