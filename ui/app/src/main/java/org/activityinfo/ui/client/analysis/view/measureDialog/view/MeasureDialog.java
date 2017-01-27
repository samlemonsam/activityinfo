package org.activityinfo.ui.client.analysis.view.measureDialog.view;

import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Dialog that lets a user choose a new measure to add to the table or chart.
 */
public class MeasureDialog {

    private final MeasureSelectionModel model;

    private final Dialog dialog;
    private final CatalogTreeView formTreeView;
    private final MeasureTypeList measureList;

    private CardLayoutContainer container;

    public MeasureDialog(FormStore formStore) {
        this.model = new MeasureSelectionModel(formStore);

        // First step of selection - a form tree view
        formTreeView = new CatalogTreeView(model.getFormStore());

        // Second step of selection, choose the measure based on the selected form
        measureList = new MeasureTypeList(model.getSelectedFormSchema());

        container = new CardLayoutContainer();
        container.add(formTreeView);
        container.add(measureList);

        model.getSelectionStep().subscribe(new Observer<MeasureSelectionModel.SelectionStep>() {
            @Override
            public void onChange(Observable<MeasureSelectionModel.SelectionStep> step) {
                switch (step.get()) {
                    case FORM:
                        container.setActiveWidget(formTreeView);
                        break;
                    case MEASURE:
                        container.setActiveWidget(measureList);
                        break;
                }
            }
        });

        TextButton previousButton = new TextButton("Previous");
        previousButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                model.previousStep();
            }
        });


        TextButton nextButton = new TextButton("Next");
        nextButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                model.selectForm(formTreeView.getSelectedFormId().get());
                model.nextStep();
            }
        });


        dialog = new Dialog();
        dialog.setHeading("Add New Measure");
        dialog.setClosable(true);
        dialog.setPixelSize(640, 480);
        dialog.setResizable(false);
        dialog.addButton(nextButton);


        dialog.add(container);


    }



    public void show() {
        dialog.show();
        dialog.center();
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {

            }
        });
    }


}
