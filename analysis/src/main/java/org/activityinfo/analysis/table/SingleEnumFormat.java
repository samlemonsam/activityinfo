package org.activityinfo.analysis.table;

import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.type.enumerated.EnumType;

public class SingleEnumFormat extends SimpleColumnFormat<String> {

    private final EnumType enumType;

    protected SingleEnumFormat(String id, FormulaNode formula, EnumType enumType) {
        super(id, formula);
        this.enumType = enumType;
    }

    public EnumType getEnumType() {
        return enumType;
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitSingleEnumColumn(columnModel, this);
    }

    @Override
    public ColumnRenderer<String> createRenderer() {
        return new StringRenderer(getId());
    }
}
