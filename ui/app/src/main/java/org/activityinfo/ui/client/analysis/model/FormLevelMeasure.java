package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

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
    public final Observable<List<DimensionSourceModel>> availableDimensions(FormStore store) {
        return store.getFormClass(formId).transform(formClass -> {
            List<DimensionSourceModel> sources = new ArrayList<>();
            sources.add(FormDimensionSource.INSTANCE);
            sources.addAll(FieldDimensionSource.sources(formClass));
            return sources;
        });
    }

}
