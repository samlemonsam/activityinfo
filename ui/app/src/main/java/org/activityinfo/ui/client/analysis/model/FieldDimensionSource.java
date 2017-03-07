package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonObject;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.util.Collections;
import java.util.Set;

/**
 * Field that can be used as a dimension source
 */
public class FieldDimensionSource extends DimensionSource {

    private String label;
    private ExprNode expr;


    public FieldDimensionSource(String label, ExprNode node) {
        this.label = label;
        this.expr = node;
    }

    public FieldDimensionSource(FormField field) {
        this(field.getLabel(), new SymbolExpr(field.getId()));
        this.label = field.getLabel();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Set<ColumnModel> getRequiredColumns(String dimensionId) {
        return Collections.singleton(new ColumnModel().setExpression(expr).setId(dimensionId));
    }


    @Override
    public DimensionReader createReader(String dimensionId, MeasureLabels measureLabels, FormClass formClass, ColumnSet input) {
        ColumnView columnView = input.getColumnView(dimensionId);
        return row -> columnView.getString(row);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "field");
        object.addProperty("label", label);
        object.addProperty("expr", expr.toString());
        return object;
    }
}
