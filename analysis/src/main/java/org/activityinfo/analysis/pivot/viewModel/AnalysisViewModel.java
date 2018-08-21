/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.analysis.pivot.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.model.analysis.TypedAnalysis;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.Maybe;
import org.activityinfo.model.analysis.pivot.DimensionModel;
import org.activityinfo.model.analysis.pivot.ImmutablePivotModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.ui.client.store.FormStore;

import java.util.List;
import java.util.logging.Logger;

/**
 * Describes an analysis and its results
 */
public class AnalysisViewModel {

    private static final Logger LOGGER = Logger.getLogger(AnalysisViewModel.class.getName());

    private final FormStore formStore;

    private String id;

    private final PivotViewModel pivotViewModel;

    private final StatefulValue<Optional<PivotModel>> draftModel;
    private final StatefulValue<DraftMetadata> draftMetadata;

    private final Observable<Maybe<TypedAnalysis<PivotModel>>> saved;
    private final Observable<WorkingModel<PivotModel>> workingModel;

    public AnalysisViewModel(FormStore formStore) {
        this(formStore, ResourceId.generateCuid());
    }

    public AnalysisViewModel(FormStore formStore, String analysisId) {
        this.id = analysisId;
        this.formStore = formStore;
        this.saved = formStore.getAnalysis(analysisId).transform(maybe -> maybe.transform(a -> {
            LOGGER.info("Saved pivot model retrieved");
            LOGGER.info("Saved Model: " + "id:"  + a.getId());
            LOGGER.info("Saved Model: " + "label:"  + a.getLabel());
            LOGGER.info("Saved Model: " + "parentId:"  + a.getParentId());
            LOGGER.info("Saved Model: " + "model (unparsed):"  + a.getModel());
            LOGGER.info("Saved Model: " + "model (parsed): " + PivotModel.fromJson(a.getModel()));
            TypedAnalysis<PivotModel> typedAnalysis =  new TypedAnalysis<PivotModel>(a.getId(), a.getLabel(), a.getParentId(), PivotModel.fromJson(a.getModel()));
            LOGGER.info("Pivot analysis model generated");
            return typedAnalysis;
        }));

        this.draftModel = new StatefulValue<>(Optional.absent());
        this.draftMetadata = new StatefulValue<>(ImmutableDraftMetadata.builder().build());

        this.workingModel = Observable.transform(saved, draftMetadata, draftModel, (saved, metadata, model) -> {
            LOGGER.info("Loaded working model");
            return new WorkingModel<PivotModel>(analysisId, saved, metadata, model, PivotViewModel.EMPTY);
        });

        // Before anything else, we need to fetch/compute the metadata required to even
        // plan the computation
        Observable<PivotModel> pivotModel = workingModel.transform(wm -> {
            LOGGER.info("Working pivot model updated");
            return wm.getModel();
        });
        this.pivotViewModel = new PivotViewModel(pivotModel, formStore);
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
        return pivotViewModel.getEffectiveModel();
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
        return pivotViewModel.getDimensions();
    }

    public Observable<FormForest> getFormForest() {
        return pivotViewModel.getFormForest();
    }

    public void addMeasure(MeasureModel measure) {
        ImmutablePivotModel newModel = ImmutablePivotModel.builder()
                .from(this.getWorkingModel())
                .addMeasures(measure)
                .build();

        updateModel(newModel);
    }


    public Observable<AnalysisResult> getResultTable() {
        return pivotViewModel.getResultTable();
    }

    public Observable<PivotTable> getPivotTable() {
        return pivotViewModel.getPivotTable();
    }

    public void updateTitle(String title) {
        draftMetadata.updateIfNotEqual(ImmutableDraftMetadata.copyOf(draftMetadata.get()).withLabel(title));
    }

    public void updateFolderId(String folderId) {
        draftMetadata.updateIfNotEqual(ImmutableDraftMetadata.copyOf(draftMetadata.get()).withFolderId(folderId));
    }
}
