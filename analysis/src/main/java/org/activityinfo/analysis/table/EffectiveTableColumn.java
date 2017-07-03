package org.activityinfo.analysis.table;

import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.type.FieldType;

import java.util.List;

public class EffectiveTableColumn {

    private TableColumn model;
    private String label;
    private ParsedFormula formula;
    private ColumnFormat format;

    public EffectiveTableColumn(FormTree formTree, TableColumn model) {
        this.model = model;
        this.formula = new ParsedFormula(formTree, model.getFormula());
        if(model.getLabel().isPresent()) {
            this.label = model.getLabel().get();
        } else {
            this.label = formula.getLabel();
        }
        format = ColumnFormatFactory.create(model.getId(), formula);
    }

    public FieldType getType() {
        return formula.getResultType();
    }

    public String getId() {
        return model.getId();
    }

    public String getLabel() {
        return label;
    }

    public List<ColumnModel> getQueryModel() {
        return format.getColumnModels();
    }

    public boolean isValid() {
        return formula.isValid();
    }

    public <T> T accept(TableColumnVisitor<T> visitor) {
        return format.accept(this, visitor);
    }

    @Override
    public String toString() {
        return "EffectiveTableColumn{" + label + "}";
    }
}
