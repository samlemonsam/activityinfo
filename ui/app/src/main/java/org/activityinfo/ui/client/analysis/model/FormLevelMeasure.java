package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

public abstract class FormLevelMeasure implements MeasureSource {

    private final ResourceId formId;

    public FormLevelMeasure(ResourceId formId) {
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
