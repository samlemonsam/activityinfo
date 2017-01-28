package org.activityinfo.ui.client.analysis.view.measureDialog.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final StatefulValue<Optional<ResourceId>> selectedFormId = new StatefulValue<>(Optional.absent());

    private final StatefulValue<Optional<MeasureType>> selectedMeasureType = new StatefulValue<>(Optional.absent());

    private final Observable<Optional<FormClass>> selectedFormSchema;

    private StatefulValue<SelectionStep> selectionStep = new StatefulValue<>(SelectionStep.FORM);


    public MeasureSelectionModel(final FormStore formStore) {
        this.formStore = formStore;
        this.selectedFormSchema = selectedFormId.join(selection -> {
            if (selection.isPresent()) {
                return formStore.getFormClass(selection.get()).transform(formClass -> Optional.of(formClass));
            } else {
                return Observable.just(Optional.absent());
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


    /**
     * @return the list of available measures, a function of the selected form.
     */
    public Observable<List<MeasureType>> getAvailableMeasures() {
        return selectedFormSchema.transform(new Function<Optional<FormClass>, List<MeasureType>>() {
            @Override
            public List<MeasureType> apply(Optional<FormClass> selectedForm) {
                if (!selectedForm.isPresent()) {
                    return Collections.emptyList();
                } else {
                    return availableMeasures(selectedForm.get());
                }
            }
        });
    }

    public Observable<MeasureType> getSelectedMeasureType() {
        return selectedMeasureType.transform(measureType -> measureType.or(new CountMeasureType()));
    }

    private List<MeasureType> availableMeasures(FormClass selectedForm) {
        List<MeasureType> measureTypes = new ArrayList<>();
        measureTypes.add(new CountMeasureType());
        measureTypes.add(new CalculationMeasureType());
        for (FormField field : selectedForm.getFields()) {
            if (field.getType() instanceof QuantityType) {
                measureTypes.add(new FieldMeasureType(selectedForm.getId(), field));
            }
        }
        return measureTypes;
    }


    public void selectForm(Optional<ResourceId> formId) {
        selectedFormId.updateIfNotEqual(formId);
    }


    public void selectMeasureType(Optional<MeasureType> measureType) {
        selectedMeasureType.updateIfNotEqual(measureType);
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
