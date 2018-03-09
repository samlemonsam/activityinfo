package org.activityinfo.analysis.table;

import org.activityinfo.model.formula.FormulaNode;

import java.util.Date;

public class DateFormat extends SimpleColumnFormat<Date> {

    protected DateFormat(String id, FormulaNode formula) {
        super(id, formula);
    }

    @Override
    public ColumnRenderer<Date> createRenderer() {
        return new DateRenderer(getId());
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitDateColumn(columnModel, this);
    }
}
