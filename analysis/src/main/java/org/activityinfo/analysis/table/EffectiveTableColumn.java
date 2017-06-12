package org.activityinfo.analysis.table;

import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.type.FieldType;

public class EffectiveTableColumn {

    private TableColumn model;
    private String label;
    private ParsedFormula formula;

    public EffectiveTableColumn(FormTree formTree, TableColumn model) {
        this.model = model;
        this.formula = new ParsedFormula(formTree, model.getFormula());
        if(model.getLabel().isPresent()) {
            this.label = model.getLabel().get();
        } else {
            this.label = formula.getLabel();
        }
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

    public ColumnModel getQueryModel() {
        return new ColumnModel().setId(getId()).setExpression(formula.getFormula());
    }

    public boolean isValid() {
        return formula.isValid();
    }
}
