package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;

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

}
