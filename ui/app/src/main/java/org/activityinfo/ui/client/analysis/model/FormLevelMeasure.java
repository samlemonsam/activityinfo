package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

public abstract class FormLevelMeasure extends MeasureModel {

    private final ResourceId formId;

    public FormLevelMeasure(String key, String label, ResourceId formId) {
        super(key, label);
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    @Override
    public final Observable<FormForest> getFormSet(FormStore store) {
        return store.getFormTree(formId).transform(FormForest::new);
    }

}
