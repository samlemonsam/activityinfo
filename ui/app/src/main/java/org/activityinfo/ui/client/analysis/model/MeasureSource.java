package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonObject;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

public interface MeasureSource {

    Observable<FormForest> getFormSet(FormStore store);

    Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions, Observable<MeasureLabels> measureLabels);

    JsonObject toJsonObject();
}
