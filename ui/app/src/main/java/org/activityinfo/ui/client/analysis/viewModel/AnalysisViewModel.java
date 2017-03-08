package org.activityinfo.ui.client.analysis.viewModel;

import com.google.gson.JsonObject;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Describes an analysis and its results
 */
public class AnalysisViewModel {

    private static final Logger LOGGER = Logger.getLogger(AnalysisViewModel.class.getName());

    private final FormStore formStore;
    private final StatefulValue<AnalysisModel> model;
    private final Observable<FormForest> formForest;
    private final Observable<AnalysisResult> resultTable;

    public AnalysisViewModel(FormStore formStore) {
        this.formStore = formStore;
        this.model = new StatefulValue<>(new AnalysisModel());

        // Before anything else, we need to fetch/compute the metadata required to even
        // plan the computation
        this.formForest = model.join(m -> {
            // Find unique list of forms involved in the analysis
            Set<ResourceId> forms = m.formIds();

            // Build a FormTree for each form
            List<Observable<FormTree>> trees = forms.stream().map(id -> formStore.getFormTree(id)).collect(Collectors.toList());

            // Combine into a FormForest
            return Observable.flatten(trees).transform(t -> new FormForest(t));
        });

        // Combine the model + metadata into the result table
        resultTable = Observable.join(model, formForest, (m, ff) -> AnalysisResult.compute(formStore, m, ff));

    }

    public void updateModel(AnalysisModel model) {
        this.model.updateValue(model);
    }

    public ObservableList<DimensionModel> getDimensions() {
        throw new UnsupportedOperationException();
    }

    public FormStore getFormStore() {
        return formStore;
    }

    /**
     * @return the list of measures present in this analysis.
     */
    public ObservableList<MeasureModel> getMeasures() {
        throw new UnsupportedOperationException();
    }

    public Observable<FormForest> getFormForest() {
        throw new UnsupportedOperationException();
    }


    public void addMeasure(MeasureModel measure) {
        beforeChange();
        throw new UnsupportedOperationException();
    }


    public void updateMeasureFormula(String measureId, String formula) {
        beforeChange();
        throw new UnsupportedOperationException();
    }

    public void updateMeasureLabel(String key, String value) {
        beforeChange();
        throw new UnsupportedOperationException();
    }

    public Observable<AnalysisResult> getResultTable() {
        return resultTable;
    }

    public void removeDimension(String id) {
        throw new UnsupportedOperationException();
    }

    public void removeMeasure(String id) {
        throw new UnsupportedOperationException();
    }

    public void beforeChange() {
        LOGGER.log(Level.INFO, "State: " + toJsonObject().toString());
    }

    public JsonObject toJsonObject() {
        throw new UnsupportedOperationException();
    }

    public void loadFromJson(JsonObject object) {



    }

    public void addDimension(DimensionModel selectedItem) {
        throw new UnsupportedOperationException();
    }
}
