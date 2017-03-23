package org.activityinfo.ui.client.analysis.viewModel;

import com.google.gson.JsonObject;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.ImmutableAnalysisModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Describes an analysis and its results
 */
public class AnalysisViewModel {

    private static final Logger LOGGER = Logger.getLogger(AnalysisViewModel.class.getName());

    private final FormStore formStore;
    private final StatefulValue<ImmutableAnalysisModel> model;
    private final Observable<FormForest> formForest;
    private final Observable<EffectiveModel> effectiveModel;
    private final Observable<AnalysisResult> resultTable;
    private final Observable<List<EffectiveDimension>> dimensions;
    private final Observable<PivotTable> pivotTable;

    public AnalysisViewModel(FormStore formStore) {
        this.formStore = formStore;
        this.model = new StatefulValue<>(ImmutableAnalysisModel.builder().build());



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

        resultTable = effectiveModel.join( m -> AnalysisResult.compute(formStore, m) );
        dimensions = effectiveModel.transform(em -> em.getDimensions());
        pivotTable = resultTable.transform(t -> new PivotTable(t));
    }

    public AnalysisModel getModel() {
        return model.get();
    }

    public Observable<EffectiveModel> getEffectiveModel() {
        return effectiveModel;
    }

    public void updateModel(AnalysisModel model) {
        this.model.updateValue(ImmutableAnalysisModel.copyOf(model));
    }

    public ObservableList<DimensionModel> getDimensions() {
        throw new UnsupportedOperationException();
    }

    public FormStore getFormStore() {
        return formStore;
    }

    public Observable<List<EffectiveDimension>> getDimensionListItems() {
        return dimensions;
    }

    public Observable<FormForest> getFormForest() {
        return formForest;
    }


    public void addMeasure(MeasureModel measure) {
        ImmutableAnalysisModel newModel = ImmutableAnalysisModel.builder()
                .from(this.model.get())
                .addMeasures(measure)
                .build();

        this.model.updateValue(newModel);
    }


    public Observable<AnalysisResult> getResultTable() {
        return resultTable;
    }

    public JsonObject toJsonObject() {
        throw new UnsupportedOperationException();
    }

    public void loadFromJson(JsonObject object) {

    }

}
