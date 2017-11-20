package org.activityinfo.analysis.table;

import org.activityinfo.model.expr.ExprNode;

public class NumberFormat extends SimpleColumnFormat<Double> {

    protected NumberFormat(String id, ExprNode formula) {
        super(id, formula);
    }

    @Override
    public ColumnRenderer<Double> createRenderer() {
        return new DoubleRenderer(getId());
    }


    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitNumberColumn(columnModel, this);
    }
}
