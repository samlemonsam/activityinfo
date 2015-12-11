package org.activityinfo.store.query.impl.eval;

import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.geo.Extents;

import java.util.Date;

public class DoubleBinaryOpView implements ColumnView {

    public enum Op {
        ADD,
        SUB,
        MUL,
        DIV;
    }
    
    private ColumnView x;
    private ColumnView y;
    private Op op;

    public DoubleBinaryOpView(String functionName, ColumnView x, ColumnView y) {
        this.x = x;
        this.y = y;
        switch (functionName) {
            case "+":
                op = Op.ADD;
                break;
            case "-":
                op = Op.SUB;
                break;
            case "*":
                op = Op.MUL;
                break;
            case "/":
                op = Op.DIV;
                break;                
        }
    }

    @Override
    public ColumnType getType() {
        return ColumnType.NUMBER;
    }

    @Override
    public int numRows() {
        return x.numRows();
    }

    @Override
    public Object get(int row) {
        return getDouble(row);
    }

    @Override
    public double getDouble(int row) {
        double dx = x.getDouble(row);
        double dy = y.getDouble(row);
        switch (op) {
            case ADD:
                return dx + dy;
            case SUB:
                return dx - dy;
            case MUL:
                return dx * dy;
            case DIV:
                return dx / dy;
            default:
                throw new UnsupportedOperationException();
        }
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
    public Extents getExtents(int row) {
        return null;
    }

    @Override
    public int getBoolean(int row) {
        return 0;
    }
}
