package org.activityinfo.ui.client.measureDialog.model;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulList;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;
import org.activityinfo.ui.client.store.FormStore;

import java.util.List;

/**
 * This model contains the state related to the user's ongoing selection of a
 * new measure.
 *
 */
public class MeasureSelectionModel {


    public enum SelectionStep {
        FORM,
        MEASURE,
        MEASURE_OPTIONS
    }

    private final FormStore formStore;

    /**
     * Forms that have been selected
     */
    private final StatefulList<ResourceId> selectedForms = new StatefulList<>();

    private final Observable<FormForest> selectedFormSet;

    private final StatefulValue<Optional<MeasureModel>> selectedMeasure = new StatefulValue<>(Optional.absent());

    private StatefulValue<SelectionStep> selectionStep = new StatefulValue<>(SelectionStep.FORM);

    public MeasureSelectionModel(final FormStore formStore) {
        this.formStore = formStore;
        Observable<List<FormTree>> flatMap = selectedForms.flatMap(formStore::getFormTree);
        this.selectedFormSet = flatMap.transform(FormForest::new);
    }


    public FormStore getFormStore() {
        return formStore;
    }

    public StatefulList<ResourceId> getSelectedForms() {
        return selectedForms;
    }

    public Observable<SelectionStep> getSelectionStep() {
        return selectionStep;
    }

    public Observable<FormForest> getSelectedFormSet() {
        return selectedFormSet;
    }

    public Observable<Optional<MeasureModel>> getSelectedMeasure() {
        return Observable.transform(getSelectedFormSet(), selectedMeasure, new BiFunction<FormForest, Optional<MeasureModel>, Optional<MeasureModel>>() {
            @Override
            public Optional<MeasureModel> apply(FormForest formForest, Optional<MeasureModel> selected) {
                if(selected.isPresent()) {
                    // TODO: ensure the measure is still valid for the form selection
                    return selected;
                } else {
                    return Optional.absent();
                }
            }
        });
    }

    public void selectForm(Optional<ResourceId> formId) {
        if(formId.isPresent()) {
            selectedForms.set(formId.get());
        } else {
            selectedForms.clear();
        }
    }


    public void selectForm(CatalogEntry form) {
        if(form.getType() == CatalogEntryType.FORM) {
            selectForm(Optional.of(ResourceId.valueOf(form.getId())));
        }
    }

    public void selectMeasure(MeasureModel measure) {
        this.selectedMeasure.updateIfNotEqual(Optional.of(measure));
    }

    public void reset() {
        selectionStep.updateIfNotEqual(SelectionStep.FORM);
        selectedForms.clear();
        selectedMeasure.clear();
    }

}
