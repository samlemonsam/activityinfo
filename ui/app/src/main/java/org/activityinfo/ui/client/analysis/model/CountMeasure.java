package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;

/**
 * Measure that resolves to a count of records in a given form.
 */
public class CountMeasure extends MeasureModel {

    private ResourceId formId;

    public CountMeasure(ResourceId formId) {
        super(ResourceId.generateCuid(), I18N.CONSTANTS.count());
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }
}
