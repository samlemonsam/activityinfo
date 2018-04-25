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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.Radio;
import org.activityinfo.analysis.table.ExportScope;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Provides user with export options.
 */
public class ExportOptionsDialog {



    private class Form {
        private ResourceId formId;
        private String label;
        private boolean subForm;

        public Form(FormClass formClass) {
            this.formId = formClass.getId();
            this.label = formClass.getLabel();
            this.subForm = false;
        }

        public Form(FormTree.Node subForm) {
            this.formId = ((SubFormReferenceType) subForm.getType()).getClassId();
            this.label = subForm.getField().getLabel();
            this.subForm = true;
        }

        public boolean isSubForm() {
            return subForm;
        }
    }

    private final FormStore formStore;
    private final TableViewModel viewModel;

    private final ListStore<Form> formListStore;
    private final ComboBox<Form> formCombo;

    private final Dialog dialog;

    private final Observable<FormTree> formTree;
    private final Observable<TableModel> exportModel;
    private final SubscriptionSet subscriptions = new SubscriptionSet();


    public ExportOptionsDialog(FormStore formStore, TableViewModel viewModel) {
        this.formStore = formStore;
        this.viewModel = viewModel;

        // Drop down to allow selecting either the parent form or one of the subforms

        formListStore = new ListStore<>(form -> form.formId.asString());
        formCombo = new ComboBox<>(formListStore, item -> item.label);
        formCombo.setEditable(false);
        formCombo.setForceSelection(true);
        formCombo.setAllowBlank(false);
        formCombo.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        // Choose between selected columns and all column
        Radio allColumnsRadio = new Radio();
        allColumnsRadio.setBoxLabel(I18N.CONSTANTS.allColumns());

        Radio selectedColumnsRadio = new Radio();
        selectedColumnsRadio.setBoxLabel(I18N.CONSTANTS.selectedColumns());
        selectedColumnsRadio.setValue(true);

        ToggleGroup columnsGroup = new ToggleGroup();
        columnsGroup.add(allColumnsRadio);
        columnsGroup.add(selectedColumnsRadio);

        HorizontalPanel columnsPanel = new HorizontalPanel();
        columnsPanel.add(allColumnsRadio);
        columnsPanel.add(selectedColumnsRadio);

        // Choose to include filter
        Radio noFilterRadio = new Radio();
        noFilterRadio.setBoxLabel(I18N.CONSTANTS.noFilter());

        Radio currentFilterRadio = new Radio();
        currentFilterRadio.setBoxLabel(I18N.CONSTANTS.currentFilter());
        currentFilterRadio.setValue(true);

        ToggleGroup filterGroup = new ToggleGroup();
        filterGroup.add(noFilterRadio);
        filterGroup.add(currentFilterRadio);

        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.add(noFilterRadio);
        filterPanel.add(currentFilterRadio);

        VerticalLayoutContainer container = new VerticalLayoutContainer();
        Margins fieldMargins = new Margins(5, 10, 5, 10);

        container.add(new FieldLabel(formCombo, I18N.CONSTANTS.form()),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1, fieldMargins));
        container.add(new FieldLabel(columnsPanel, I18N.CONSTANTS.columns()),
                new VerticalLayoutContainer.VerticalLayoutData(-1, -1, fieldMargins));
        container.add(new FieldLabel(filterPanel, I18N.CONSTANTS.filter()),
                new VerticalLayoutContainer.VerticalLayoutData(-1,-1, fieldMargins));

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.export());
        dialog.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);
        dialog.setPixelSize(500, 300);
        dialog.setModal(true);
        dialog.setWidget(container);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOk);
        dialog.getButton(Dialog.PredefinedButton.CANCEL).addSelectHandler(this::onCancel);
        dialog.addDialogHideHandler(this::onDialogHide);

        Observable<ResourceId> selectedForm = GxtObservables.of(formCombo).transform(form -> form.formId);
        Observable<ExportScope> columnScope = GxtObservables.of(selectedColumnsRadio).transform(checked -> checked ? ExportScope.SELECTED : ExportScope.ALL);
        Observable<ExportScope> rowScope = GxtObservables.of(currentFilterRadio).transform(checked -> checked ? ExportScope.SELECTED : ExportScope.ALL);

        this.formTree = viewModel.getFormTree();
        this.exportModel = viewModel.computeExportModel(selectedForm, columnScope, rowScope);
    }


    public void show() {
        dialog.show();
        subscriptions.add(formTree.subscribe(this::onFormTreeChanged));
        subscriptions.add(exportModel.subscribe(this::onExportModelChanged));
    }


    private void onFormTreeChanged(Observable<FormTree> formTree) {

        formListStore.clear();
        formCombo.setEnabled(formTree.isLoaded());
        dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(formTree.isLoaded());

        if(formTree.isLoaded()) {
            formListStore.add(new Form(formTree.get().getRootFormClass()));

            for (FormTree.Node rootField : formTree.get().getRootFields()) {
                if(rootField.isSubForm()) {
                    formListStore.add(new Form(rootField));
                }
            }

            if(formCombo.getValue() == null) {
                formCombo.setValue(formListStore.get(0));
            }
        }
    }


    private void onExportModelChanged(Observable<TableModel> exportModel) {
        dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(exportModel.isLoaded());
    }

    private void onCancel(SelectEvent event) {
        dialog.hide();
    }

    private void onDialogHide(DialogHideEvent dialogHideEvent) {
        subscriptions.unsubscribeAll();
    }

    private void onOk(SelectEvent event) {

        ExportFormJob exportFormJob = new ExportFormJob(exportModel.get());
        dialog.hide();

        Observable<JobStatus<ExportFormJob, ExportResult>> jobStatus = formStore.startJob(exportFormJob);
        ExportJobDialog statusDialog = new ExportJobDialog(jobStatus);
        statusDialog.show();
    }

}
