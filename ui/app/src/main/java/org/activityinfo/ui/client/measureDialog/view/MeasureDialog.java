package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Optional;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Dialog that lets a user choose a new measure to add to the table or chart.
 */
public class MeasureDialog implements HasSelectionHandlers<MeasureModel> {


    private final MeasureSelectionModel model;

    private final Dialog dialog;


    FormTreeView formTree;
    FieldTreeView fieldTree;


    FormulaPanel formulaPanel;

    private SimpleEventBus eventBus = new SimpleEventBus();

    public MeasureDialog(FormStore formStore) {
        model = new MeasureSelectionModel(formStore);


        // Step 1: Select the form to add
        TabItemConfig formTreeTab = new TabItemConfig("Choose Form");
        formTree = new FormTreeView(model.getFormStore());
        formTree.addSelectionHandler(event -> model.selectForm(event.getSelectedItem()));

        // Step 2: Select fields to add
        TabItemConfig fieldTab = new TabItemConfig("Choose Field");
        fieldTree = new FieldTreeView(model.getSelectedFormSet());
        fieldTree.getSelectionModel().addSelectionHandler(event ->
                model.selectMeasure(event.getSelectedItem().newMeasure()));


        // Tab Panel
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(formTree,  formTreeTab);
        tabPanel.add(fieldTree, fieldTab);
//        tabPanel.add(formulaPanel, formulaTab);

        this.dialog = new Dialog();
        this.dialog.setHeading("Add New Measure");
        this.dialog.setBodyBorder(false);
        this.dialog.setPixelSize(640, 480);
        this.dialog.setClosable(true);
        this.dialog.setWidget(tabPanel);
        this.dialog.setModal(true);

        model.getSelectedForms().asObservable().subscribe(selection -> {
            fieldTab.setEnabled(selection.isLoaded() && !selection.get().isEmpty());
            tabPanel.update(fieldTree.asWidget(), fieldTab);
        });

        model.getSelectedMeasure().subscribe(selection -> {
            boolean ready = selection.isLoaded() && selection.get().isPresent();
            dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(ready);
        });

        fieldTree.getCalculateButton().addSelectHandler(this::onCalculate);

        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOK);
    }

    private void onCalculate(SelectEvent event) {
        FormulaDialog dialog = new FormulaDialog(model.getFormStore(), model.getSelectedForms().getList().get(0));
        dialog.show();
    }


    public void onOK(SelectEvent event) {
        Observable<Optional<MeasureModel>> measure = model.getSelectedMeasure();
        if(measure.isLoaded() && measure.get().isPresent()) {
            SelectionEvent.fire(this, measure.get().get());
            dialog.hide();
        }
    }

    public void show() {
        model.reset();
        dialog.show();
        dialog.center();
    }


    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<MeasureModel> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {
        eventBus.fireEvent(gwtEvent);
    }
}
