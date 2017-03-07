package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;

/**
 * The name of a form that is the dimension
 */
public class FormDimensionSource extends DimensionSource {

    public static final FormDimensionSource INSTANCE = new FormDimensionSource();

    private FormDimensionSource() {
    }

    @Override
    public String getLabel() {
        return I18N.CONSTANTS.form();
    }


    @Override
    public DimensionReader createReader(String dimensionId, MeasureLabels measureLabels, FormClass formClass, ColumnSet input) {
        return row -> formClass.getLabel();
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "form");
        return object;
    }
}
