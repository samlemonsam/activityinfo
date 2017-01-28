package org.activityinfo.ui.client.analysis.view.measureDialog.view;

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureType;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Dialog that lets a user choose a new measure to add to the table or chart.
 */
public class MeasureDialog {

    interface MyUiBinder extends UiBinder<Dialog, MeasureDialog> {
    }

    private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final MeasureSelectionModel model;

    private final Dialog dialog;

    @UiField
    CardLayoutContainer container;

    @UiField(provided = true)
    CatalogTreeView formTree;

    @UiField(provided = true)
    MeasureTypeListView measureList;


    public MeasureDialog(FormStore formStore) {
        model = new MeasureSelectionModel(formStore);
        measureList = new MeasureTypeListView(model);
        formTree = new CatalogTreeView(model.getFormStore());

        this.dialog = uiBinder.createAndBindUi(this);

        model.getSelectionStep().subscribe(step -> {
            switch (step.get()) {
                case FORM:
                    container.setActiveWidget(formTree);
                    break;
                case MEASURE:
                    container.setActiveWidget(measureList);
                    break;
            }
        });

    }

    @UiHandler("previousButton")
    void onPrevious(SelectEvent event) {
        model.previousStep();
    }

    @UiHandler("nextButton")
    void onNext(SelectEvent event) {
        model.selectForm(formTree.getSelectedFormId().get());
        model.nextStep();
    }

    @UiHandler("measureList")
    public void onMeasureSelected(SelectionEvent<MeasureType> event) {
        model.selectMeasureType(Optional.fromNullable(event.getSelectedItem()));
    }


    public void show() {
        dialog.show();
        dialog.center();
    }
}
