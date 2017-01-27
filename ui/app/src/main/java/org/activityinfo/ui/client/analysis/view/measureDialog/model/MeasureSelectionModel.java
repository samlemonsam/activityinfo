package org.activityinfo.ui.client.analysis.view.measureDialog.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;

/**
 * This model contains the state related to the user's ongoing selection of a
 * new measure.
 *
 */
public class MeasureSelectionModel {


    public enum SelectionStep {
        FORM,
        MEASURE
    }


    private final FormStore formStore;

    private final StatefulValue<Optional<ResourceId>> selectedFormId = new StatefulValue<>(Optional.<ResourceId>absent());

    private final Observable<Optional<FormClass>> selectedFormSchema;

    private StatefulValue<SelectionStep> selectionStep = new StatefulValue<>(SelectionStep.FORM);

    public MeasureSelectionModel(final FormStore formStore) {
        this.formStore = formStore;
        this.selectedFormSchema = selectedFormId.join(new Function<Optional<ResourceId>, Observable<Optional<FormClass>>>() {
            @Override
            public Observable<Optional<FormClass>> apply(Optional<ResourceId> selectedFormId) {
                if(selectedFormId.isPresent()) {
                    return formStore.getFormClass(selectedFormId.get()).transform(new Function<FormClass, Optional<FormClass>>() {
                        @Override
                        public Optional<FormClass> apply(FormClass formClass) {
                            return Optional.of(formClass);
                        }
                    });
                } else {
                    return Observable.just(Optional.<FormClass>absent());
                }
            }
        });
    }


    public FormStore getFormStore() {
        return formStore;
    }

    public Observable<SelectionStep> getSelectionStep() {
        return selectionStep;
    }

    public Observable<Optional<FormClass>> getSelectedFormSchema() {
        return selectedFormSchema;
    }



    public void selectForm(Optional<ResourceId> formId) {
        selectedFormId.updateValue(formId);
    }

    /**
     * Advances to the next step if possible.
     */
    public void nextStep() {

        switch (selectionStep.get()) {
            case FORM:
                if(selectedFormId.get().isPresent()) {
                    selectionStep.updateValue(SelectionStep.MEASURE);
                }
                break;
            case MEASURE:
                break;
        }
    }


    /**
     * Retreats to the previous step if possible
     */
    public void previousStep() {
        switch (selectionStep.get()) {
            case FORM:
                break;
            case MEASURE:
                selectionStep.updateValue(SelectionStep.FORM);
        }
    }

}
