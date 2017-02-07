package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;

/**
 * The name of a form that is the dimension
 */
public class FormDimensionSource extends DimensionSourceModel {

    public static final FormDimensionSource INSTANCE = new FormDimensionSource();

    private FormDimensionSource() {
    }

    @Override
    public String getLabel() {
        return I18N.CONSTANTS.form();
    }

    @Override
    public DimensionReader createReader(String dimensionId, FormClass formClass, ColumnSet input) {
        return row -> formClass.getLabel();
    }
}
