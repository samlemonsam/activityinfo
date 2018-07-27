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
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.model.analysis.pivot.ImmutableMeasureModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.model.analysis.pivot.Statistic;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Dialog that lets a user choose a new measure to add to the table or chart.
 */
public class MeasureDialog implements HasSelectionHandlers<MeasureModel> {


    private final MeasureSelectionModel model;

    private final Dialog dialog;


    CatalogTreeView formTree;
    FieldTreeView fieldTree;

    private SimpleEventBus eventBus = new SimpleEventBus();

    public MeasureDialog(FormStore formStore) {
        model = new MeasureSelectionModel(formStore);


        // Step 1: Select the form to add
        TabItemConfig formTreeTab = new TabItemConfig("Choose Form");
        formTree = new CatalogTreeView(model.getFormStore(), Optional.absent(),
            entry -> entry.getType() != CatalogEntryType.ANALYSIS);

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




    public void onOK(SelectEvent event) {
        Observable<Optional<MeasureModel>> measure = model.getSelectedMeasure();
        if(measure.isLoaded() && measure.get().isPresent()) {
            select(measure.get().get());
        }
    }

    private void onCalculate(SelectEvent event) {
        ResourceId selectedFormId = model.getSelectedForms().getList().get(0);
        FormulaDialog dialog = new FormulaDialog(model.getFormStore(), selectedFormId);
        dialog.show(null, expr -> {
            ImmutableMeasureModel measure = ImmutableMeasureModel.builder()
                .formId(selectedFormId)
                .formula(expr.getFormula())
                .label(expr.getFormula())
                .addStatistics(Statistic.SUM)
                .build();

            select(measure);

        });
    }


    private void select(MeasureModel measure) {
        SelectionEvent.fire(this, measure);
        dialog.hide();
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
