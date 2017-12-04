package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.Radio;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.ExportFormJob;
import org.activityinfo.model.job.ExportResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Provides user with export options.
 */
public class ExportOptionsDialog {


    private final Radio selectedColumnsRadio;

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
    private Subscription formSubscription;


    private final Dialog dialog;


    public ExportOptionsDialog(FormStore formStore, TableViewModel viewModel) {
        this.formStore = formStore;
        this.viewModel = viewModel;

        // Drop down to allow selecting either the parent form or one of the subforms

        formListStore = new ListStore<>(form -> form.formId.asString());
        formCombo = new ComboBox<Form>(formListStore, item -> item.label);
        formCombo.setEditable(false);
        formCombo.setForceSelection(true);
        formCombo.setAllowBlank(false);
        formCombo.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        // Choose between selected columns and all column
        Radio allColumnsRadio = new Radio();
        allColumnsRadio.setBoxLabel(I18N.CONSTANTS.allColumns());

        selectedColumnsRadio = new Radio();
        selectedColumnsRadio.setBoxLabel(I18N.CONSTANTS.selectedColumns());
        selectedColumnsRadio.setValue(true);

        ToggleGroup columnsGroup = new ToggleGroup();
        columnsGroup.add(allColumnsRadio);
        columnsGroup.add(selectedColumnsRadio);

        HorizontalPanel columnsPanel = new HorizontalPanel();
        columnsPanel.add(allColumnsRadio);
        columnsPanel.add(selectedColumnsRadio);


        VerticalLayoutContainer container = new VerticalLayoutContainer();
        Margins fieldMargins = new Margins(5, 10, 5, 10);

        container.add(new FieldLabel(formCombo, I18N.CONSTANTS.form()),
                new VerticalLayoutContainer.VerticalLayoutData(1, -1, fieldMargins));
        container.add(new FieldLabel(columnsPanel, I18N.CONSTANTS.columns()),
                new VerticalLayoutContainer.VerticalLayoutData(-1, -1, fieldMargins));

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.export());
        dialog.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);
        dialog.setPixelSize(500, 300);
        dialog.setModal(true);
        dialog.setWidget(container);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOk);
        dialog.getButton(Dialog.PredefinedButton.CANCEL).addSelectHandler(this::onCancel);
        dialog.addDialogHideHandler(this::onDialogHide);
    }


    public void show() {
        dialog.show();
        formSubscription = viewModel.getFormTree().subscribe(this::onFormTreeChanged);
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

    private void onCancel(SelectEvent event) {
        dialog.hide();
    }

    private void onDialogHide(DialogHideEvent dialogHideEvent) {
        formSubscription.unsubscribe();
        formSubscription = null;
    }

    private void onOk(SelectEvent event) {

        ExportFormJob exportFormJob = new ExportFormJob(buildModel());
        dialog.hide();

        Observable<JobStatus<ExportFormJob, ExportResult>> jobStatus = formStore.startJob(exportFormJob);
        ExportJobDialog statusDialog = new ExportJobDialog(jobStatus);
        statusDialog.show();
    }

    private TableModel buildModel() {
        TableModel currentModel = viewModel.getTableModel().get();
        Form selectedForm = formCombo.getValue();

        if(selectedForm.isSubForm()) {
            ImmutableTableModel.Builder model = ImmutableTableModel.builder();
            model.formId(selectedForm.formId);

            return model.build();

        } else {
            ImmutableTableModel.Builder model = ImmutableTableModel.builder();
            model.formId(currentModel.getFormId());
            if(selectedColumnsRadio.getValue() == Boolean.TRUE) {
                model.columns(currentModel.getColumns());
            }
            return model.build();
        }
    }
}
