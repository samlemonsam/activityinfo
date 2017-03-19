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
import java.util.Optional;
import java.util.Set;
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
    private final Observable<EffectiveModel> effectiveModel;
    private final Observable<AnalysisResult> resultTable;
    private final Observable<List<DimensionListItem>> dimensionListItems;
            ;

    public AnalysisViewModel(FormStore formStore) {
        this.formStore = formStore;
        this.model = new StatefulValue<>(new AnalysisModel());



        // Before anything else, we need to fetch/compute the metadata required to even
        // plan the computation
        formForest = model.join(m -> {
            // Find unique list of forms involved in the analysis
            Set<ResourceId> forms = m.formIds();

            // Build a FormTree for each form
            List<Observable<FormTree>> trees = forms.stream().map(id -> formStore.getFormTree(id)).collect(Collectors.toList());

            // Combine into a FormForest
            return Observable.flatten(trees).transform(t -> new FormForest(t));
        });

        effectiveModel = Observable.transform(formForest, model, (ff, m) -> new EffectiveModel(m, ff));
        dimensionListItems = effectiveModel.transform(DimensionListItem::compute);

        resultTable = effectiveModel.join( m -> AnalysisResult.compute(formStore, m) );
    }

    public Observable<EffectiveModel> getEffectiveModel() {
        return effectiveModel;
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

    public Observable<List<DimensionListItem>> getDimensionListItems() {
        return dimensionListItems;
    }

    public Observable<FormForest> getFormForest() {
        return formForest;
    }


    public void addMeasure(MeasureModel measure) {
        beforeChange();

        AnalysisModel model = this.model.get();
        model.getMeasures().add(measure);

        this.model.updateValue(model);
    }

    public void updateMeasureFormula(String measureId, String formula) {
        beforeChange();
        throw new UnsupportedOperationException();
    }

    public void updateMeasureLabel(String measureId, String newLabel) {
        beforeChange();
        Optional<MeasureModel> measure = model.get().getMeasures().stream().filter(m -> m.getId().equals(measureId)).findFirst();
        if(measure.isPresent()) {
            measure.get().setLabel(newLabel);
            model.updated();
        }
    }

    public Observable<AnalysisResult> getResultTable() {
        return resultTable;
    }

    public void removeDimension(String id) {
        model.get().getDimensions().removeIf(d -> d.getId().equals(id));
        model.updated();
    }

    public void removeMeasure(String id) {
        beforeChange();
        model.get().getMeasures().removeIf(m -> m.getId().equals(id));
        model.updated();
    }

    public void beforeChange() {
       // LOGGER.log(Level.INFO, "State: " + toJsonObject().toString());
    }

    public JsonObject toJsonObject() {
        throw new UnsupportedOperationException();
    }

    public void loadFromJson(JsonObject object) {

    }

    public void addDimension(DimensionModel dimensionModel) {
        model.get().getDimensions().add(dimensionModel);
        model.updated();
    }
}
