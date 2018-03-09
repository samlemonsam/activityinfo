package org.activityinfo.analysis.table;

import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.query.ColumnModel;

import java.util.Collections;
import java.util.List;

public abstract class SimpleColumnFormat<T> implements ColumnFormat {

    private String id;
    private FormulaNode formula;

    public String getId() {
        return id;
    }

    protected SimpleColumnFormat(String id, FormulaNode formula) {
        this.id = id;
        this.formula = formula;
    }

    public abstract ColumnRenderer<T> createRenderer();


    @Override
    public final List<ColumnModel> getColumnModels() {
        ColumnModel columnModel = new ColumnModel();
        columnModel.setId(id);
        columnModel.setFormula(formula);

        return Collections.singletonList(columnModel);
    }

}
