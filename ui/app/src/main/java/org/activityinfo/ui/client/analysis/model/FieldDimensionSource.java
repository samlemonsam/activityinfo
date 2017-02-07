package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
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

    private ResourceId fieldId;
    private String label;


    public FieldDimensionSource(FormField field) {
        this.fieldId = field.getId();
        this.label = field.getLabel();
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
        return Collections.singleton(new ColumnModel().setExpression(new SymbolExpr(fieldId)).setId(dimensionId));
    }

    @Override
    public DimensionReader createReader(String dimensionId, FormClass formClass, ColumnSet input) {
        ColumnView columnView = input.getColumnView(dimensionId);
        return columnView::getString;
    }
}
