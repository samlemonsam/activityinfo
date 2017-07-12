package org.activityinfo.ui.client.analysis.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.analysis.TypedAnalysis;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.ImmutablePivotModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.model.PivotModel;
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

    private static final PivotModel EMPTY = ImmutablePivotModel.builder().build();

    private final FormStore formStore;

    private String id;
    private final Observable<Maybe<TypedAnalysis<PivotModel>>> saved;

    private final StatefulValue<Optional<PivotModel>> draftModel;
    private final StatefulValue<DraftMetadata> draftMetadata;

    private final Observable<WorkingModel<PivotModel>> workingModel;

    private final Observable<FormForest> formForest;
    private final Observable<EffectiveModel> effectiveModel;
    private final Observable<AnalysisResult> resultTable;
    private final Observable<List<EffectiveDimension>> dimensions;
    private final Observable<PivotTable> pivotTable;


    public AnalysisViewModel(FormStore formStore) {
        this(formStore, ResourceId.generateCuid());
    }

    public AnalysisViewModel(FormStore formStore, String analysisId) {
        this.formStore = formStore;
        this.saved = formStore.getAnalysis(analysisId).transform(maybe -> maybe.transform(a -> {
            return new TypedAnalysis<PivotModel>(a.getId(), a.getLabel(), a.getParentId(), PivotModel.fromJson(a.getModel()));
        }));


        this.draftModel = new StatefulValue<>(Optional.absent());
        this.draftMetadata = new StatefulValue<>(ImmutableDraftMetadata.builder().build());

        this.workingModel = Observable.transform(saved, draftMetadata, draftModel, (saved, metadata, model) -> {
            LOGGER.info("Loaded working model");
            return new WorkingModel<PivotModel>(analysisId, saved, metadata, model, EMPTY);
        });


        // Before anything else, we need to fetch/compute the metadata required to even
        // plan the computation
        formForest = workingModel.join(wm -> {

            PivotModel m = wm.getModel();

            // Find unique list of forms involved in the analysis
            Set<ResourceId> forms = m.formIds();

            // Build a FormTree for each form
            List<Observable<FormTree>> trees = forms.stream().map(id -> formStore.getFormTree(id)).collect(Collectors.toList());

            // Combine into a FormForest
            return Observable.flatten(trees).transform(t -> new FormForest(t));
        });

        this.id = analysisId;

        effectiveModel = Observable.transform(formForest, workingModel, (ff, m) -> new EffectiveModel(m.getModel(), ff));

        resultTable = effectiveModel.join( m -> AnalysisResult.compute(formStore, m) );
        dimensions = effectiveModel.transform(em -> em.getDimensions());
        pivotTable = resultTable.transform(t -> new PivotTable(t));
    }

    public String getId() {
        return id;
    }

    public Observable<WorkingModel<PivotModel>> getWorking() {
        return workingModel;
    }

    public PivotModel getWorkingModel() {
        return workingModel.get().getModel();
    }

    public Observable<EffectiveModel> getEffectiveModel() {
        return effectiveModel;
    }

    public PivotModel updateModel(PivotModel model) {

        LOGGER.info("model: " + model.toJson().toJson());

        this.draftModel.updateValue(Optional.of(ImmutablePivotModel.copyOf(model)));
        return model;
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
        ImmutablePivotModel newModel = ImmutablePivotModel.builder()
                .from(this.getWorkingModel())
                .addMeasures(measure)
                .build();

        updateModel(newModel);
    }


    public Observable<AnalysisResult> getResultTable() {
        return resultTable;
    }

    public Observable<PivotTable> getPivotTable() {
        return pivotTable;
    }

    public void updateTitle(String title) {
        draftMetadata.updateIfNotEqual(ImmutableDraftMetadata.copyOf(draftMetadata.get()).withLabel(title));
    }

    public void updateFolderId(String folderId) {
        draftMetadata.updateIfNotEqual(ImmutableDraftMetadata.copyOf(draftMetadata.get()).withFolderId(folderId));
    }
}
