package org.activityinfo.analysis.table;

import org.activityinfo.model.formula.FormulaNode;

/**
 * Simple column format for boolean field.
 */
public class BooleanFormat extends SimpleColumnFormat<Boolean> {

    public BooleanFormat(String id, FormulaNode formula) {
        super(id, formula);
    }

    @Override
    public ColumnRenderer<Boolean> createRenderer() {
        return new BooleanRenderer(getId());
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitBooleanColumn(columnModel, this);
    }
}
