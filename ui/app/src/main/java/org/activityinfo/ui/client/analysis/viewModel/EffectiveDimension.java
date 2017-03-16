package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import org.activityinfo.model.expr.functions.date.MonthFunction;
import org.activityinfo.model.expr.functions.date.QuarterFunction;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.ui.client.analysis.model.DimensionMapping;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.formulaDialog.ParsedFormula;

import java.util.Collections;
import java.util.List;

/**
 * Models a measure that is part of the analysis,
 * based on the user's model as well as metadata
 */
public class EffectiveDimension {
    private int index;
    private DimensionModel model;
    private DimensionMapping mapping;
    private ParsedFormula formula;

    public EffectiveDimension(FormTree formTree, int index, DimensionModel model, DimensionMapping mapping) {
        this.index = index;
        this.model = model;
        this.mapping = mapping;
        if(this.mapping != null) {
            this.formula = new ParsedFormula(formTree, mapping.getFormula());
        }
    }

    public int getIndex() {
        return index;
    }

    public DimensionMapping getMapping() {
        return mapping;
    }

    public ParsedFormula getFormula() {
        return formula;
    }

    public List<ColumnModel> getRequiredColumns() {
        if(this.mapping != null && this.formula.isValid()) {
            ColumnModel columnModel = new ColumnModel();
            columnModel.setId(getColumnId());
            columnModel.setExpression(this.formula.getFormula());
            return Collections.singletonList(columnModel);
        }
        return Collections.emptyList();
    }

    private String getColumnId() {
        return "d" + index;
    }

    public DimensionReader createReader(ColumnSet columnSet) {
        ColumnView columnView = columnSet.getColumnView(getColumnId());
        if(columnView == null) {
            return null;
        }

        Function<String, String> map = createMap();

        return row -> {
            String category = columnView.getString(row);
            if(category == null) {
                return null;
            }
            return map.apply(category);
        };
    }

    private Function<String, String> createMap() {
        if(formula.getResultType() instanceof LocalDateType) {
            switch (model.getDateLevel()) {
                case YEAR:
                    return EffectiveDimension::year;
                case MONTH:
                    return EffectiveDimension::month;
                case QUARTER:
                    return EffectiveDimension::quarter;
            }
        }
        return Functions.identity();
    }

    private static String month(String date) {
        return Integer.toString(MonthFunction.fromIsoString(date));
    }

    private static String quarter(String date) {
        int month = MonthFunction.fromIsoString(date);
        return "Q" + QuarterFunction.fromMonth(month);
    }

    private static String year(String date) {
        return date.substring(0, 4);
    }
}
