package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulList;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Describes an analysis and its results
 */
public class AnalysisModel {

    private final FormStore formStore;
    private final StatefulList<MeasureModel> measures = new StatefulList<>();
    private final Observable<AnalysisResult> result;

    public AnalysisModel(FormStore formStore) {
        this.formStore = formStore;
        this.result = compute();
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


    private Observable<AnalysisResult> compute() {
        return getMeasures()
                .map(this::computeMeasure)
                .asObservable()
                .join(Observable::flatten)
                .transform(AnalysisResult::new);

    }

    private Observable<MeasureResultSet> computeMeasure(MeasureModel model) {
        return model.compute(formStore);
    }

    public Observable<AnalysisResult> getResult() {
        return result;
    }
}
