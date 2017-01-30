package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.ArrayList;
import java.util.List;

/**
 * Field that can be used as a dimension source
 */
public class FieldDimensionSource extends DimensionSourceModel {

    private FormField field;


    public FieldDimensionSource(FormField field) {
        this.field = field;
    }

    @Override
    public String getLabel() {
        return field.getLabel();
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

}
