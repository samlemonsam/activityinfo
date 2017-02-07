package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Field that can be used as a dimension source
 */
public class FieldDimensionSource extends DimensionSourceModel {

    private String label;
    private ExprNode expr;

    public FieldDimensionSource(FormField field) {
        this.label = field.getLabel();
        this.expr = new SymbolExpr(field.getId());
    }

    public FieldDimensionSource(String label, ExprNode node) {
        this.label = label;
        this.expr = node;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static List<DimensionSourceModel> sources(FormClass formClass) {
        List<DimensionSourceModel> sources = new ArrayList<>();
        for (FormField field : formClass.getFields()) {
            if (isPotentialSource(field)) {
                sources.add(new FieldDimensionSource(field));
            }
        }
        return sources;
    }

    private static boolean isPotentialSource(FormField field) {
        return field.getType() instanceof TextType ||
                field.getType() instanceof EnumType;
    }

    @Override
    public Set<ColumnModel> getRequiredColumns(String dimensionId) {
        return Collections.singleton(new ColumnModel().setExpression(expr).setId(dimensionId));
    }

    @Override
    public DimensionReader createReader(String dimensionId, FormClass formClass, ColumnSet input) {
        ColumnView columnView = input.getColumnView(dimensionId);
        return row -> columnView.getString(row);
    }
}
