package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

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

    @Override
    public Observable<MeasureResultSet> compute(FormStore store) {
        QueryModel model = new QueryModel(formId);
        model.selectResourceId().as("id");

        return store.query(model).transform(columnSet ->
                new MeasureResultSet(new Point(columnSet.getNumRows())));
    }
}
