package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;

import java.util.Date;

public class JoinedColumnView implements ColumnView {
    private ColumnView columnView;
    private int joinMap[];

    public JoinedColumnView(ColumnView columnView, int[] joinMap) {
        this.columnView = columnView;
        this.joinMap = joinMap;
    }

    @Override
    public ColumnType getType() {
        return columnView.getType();
    }

    @Override
    public int numRows() {
        return joinMap.length;
    }

    @Override
    public Object get(int row) {
        int right = joinMap[row];
        if(right != -1) {
            return columnView.get(right);
        }
        return null;
    }

    @Override
    public double getDouble(int row) {
        int right = joinMap[row];
        if(right != -1) {
            return columnView.getDouble(right);
        }
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        int right = joinMap[row];
        if(right != -1) {
            return columnView.getString(right);
        }
        return null;
    }

    @Override
    public Date getDate(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBoolean(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i!=numRows();++i) {
            if(i>0) {
                sb.append(", ");
            }
            sb.append(getString(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
