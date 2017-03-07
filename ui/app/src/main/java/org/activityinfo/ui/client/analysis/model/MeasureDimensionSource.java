package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;

public class MeasureDimensionSource extends DimensionSource {
    @Override
    public String getLabel() {
        return I18N.CONSTANTS.measures();
    }

    @Override
    public DimensionReader createReader(String dimensionId, MeasureLabels measureLabels, FormClass formClass, ColumnSet input) {
        final String label = measureLabels.getLabel();
        return new DimensionReader() {
            @Override
            public String read(int row) {
                return label;
            }
        };
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject();
    }
}
