package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Describes an analysis and its results
 */
public class AnalysisModel {

    private final FormStore formStore;
    private final StatefulList<MeasureModel> measures;
    private final Observable<FormForest> formForest;
    private final StatefulValue<DimensionSet> dimensions;
    private final Observable<AnalysisResult> result;

    public AnalysisModel(FormStore formStore) {
        this.formStore = formStore;
        measures = new StatefulList<>();
        dimensions = new StatefulValue<>(new DimensionSet());

        // The dimension sources are a function of the measures present in the analysis
        this.formForest = measures
                .flatMap(measure -> measure.getFormSet(formStore))
                .transform(FormForest::merge);

        // The results are a function of the selected measures and dimensions
        this.result = measures
                .flatMap(measure -> measure.compute(formStore, dimensions))
                .transform(AnalysisResult::new);

    }

    public FormStore getFormStore() {
        return formStore;
    }

    /**
     * @return the list of measures present in this analysis.
     */
    public ObservableList<MeasureModel> getMeasures() {
        return measures;
    }

    public Observable<FormForest> getFormForest() {
        return formForest;
    }

    public Observable<DimensionSet> getDimensions() {
        return dimensions;
    }

    public void addMeasure(MeasureModel measure) {
        measures.add(measure);
    }

    public void addDimension(DimensionSourceModel dimensionSource) {

        DimensionModel newDimension = new DimensionModel(ResourceId.generateCuid(), dimensionSource);
        DimensionSet set = dimensions.get().add(newDimension);

        dimensions.updateValue(set);
    }

    public Observable<AnalysisResult> getResult() {
        return result;
    }


}
