package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Preconditions;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;

public class StringComparisonView implements ColumnView {

    private final ColumnView x;
    private final ColumnView y;
    private final ComparisonOp function;
    private final int numRows;

    public StringComparisonView(ColumnView x, ColumnView y, ComparisonOp function) {
        this.x = x;
        this.y = y;
        this.function = function;

        Preconditions.checkArgument(x.numRows() == y.numRows(), "Arguments must have equal lengths");
        this.numRows = x.numRows();
    }


    @Override
    public ColumnType getType() {
        return ColumnType.BOOLEAN;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return getDouble(row);
    }

    @Override
    public double getDouble(int row) {
        int b = getBoolean(row);
        if(b == NA) {
            return Double.NaN;
        } else {
            return b;
        }
    }

    @Override
    public String getString(int row) {
        int z = getBoolean(row);
        switch(z) {
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NA:
                return "NA";
        }
        return null;
    }

    @Override
    public Date getDate(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        String x = this.x.getString(row);
        String y = this.y.getString(row);
        if(x == null || y == null) {
            return NA;
        } else {
            int cmp = x.compareToIgnoreCase(y);
            return function.apply(cmp) ? 1 : 0;
        }
    }
}
