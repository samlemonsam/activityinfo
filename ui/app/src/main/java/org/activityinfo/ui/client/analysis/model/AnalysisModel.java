package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulList;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Describes an analysis and its results
 */
public class AnalysisModel {

    private final FormStore formStore;
    private final StatefulList<MeasureModel> measures = new StatefulList<>();

    public AnalysisModel(FormStore formStore) {
        this.formStore = formStore;
    }

    public FormStore getFormStore() {
        return formStore;
    }

    public ObservableList<MeasureModel> getMeasures() {
        return measures;
    }


    public void addMeasure(MeasureModel measure) {
        measures.add(measure);
    }
}
