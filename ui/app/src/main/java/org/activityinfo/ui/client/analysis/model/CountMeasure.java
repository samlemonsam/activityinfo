package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Measure that resolves to a count of records in a given form.
 */
public class CountMeasure extends FormLevelMeasure {

    private ResourceId formId;

    public CountMeasure(ResourceId formId, String label) {
        super(ResourceId.generateCuid(), label, formId);
        this.formId = formId;
    }

    @Override
    public Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions) {
        return FieldMeasure.compute(store, formId, new ConstantExpr(1), dimensions);
    }
}
