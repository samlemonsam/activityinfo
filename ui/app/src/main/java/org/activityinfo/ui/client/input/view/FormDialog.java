package org.activityinfo.ui.client.input.view;


import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.store.FormStore;

public class FormDialog {

    private ResourceId formId;
    private FormInputView panel;
    private final Dialog dialog;
    private final ConfirmMessageBox saveWarning;
    private boolean okPressed = false;

    public FormDialog(FormStore formStore, RecordRef recordRef) {
        this.formId = formId;

        panel = new FormInputView(formStore, recordRef);

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.form());
        dialog.setPixelSize(640, 480);
        dialog.setModal(true);
        dialog.setWidget(panel);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOk);
        dialog.addDialogHideHandler(this::onHide);

        saveWarning = new ConfirmMessageBox(I18N.CONSTANTS.warning(), I18N.CONSTANTS.promptSave());
        saveWarning.setModal(true);
        saveWarning.getButton(ConfirmMessageBox.PredefinedButton.YES).addSelectHandler(yes -> panel.save(closeHandler -> close()));
        saveWarning.getButton(ConfirmMessageBox.PredefinedButton.NO).addSelectHandler(no -> close());
        saveWarning.hide();
    }

    // User has pressed "x", or has saved the form (i.e. pressed OK)
    private void onHide(DialogHideEvent hide) {
        if (!okPressed) {
            saveWarning.show();
        }
    }

    // User has pressed "OK"
    private void onOk(SelectEvent ok) {
        panel.save(closeEvent -> {
            okPressed = true;
            close();
        });
    }

    public void close() {
        saveWarning.hide();
        dialog.hide();
    }

    public void show() {
        dialog.show();
        dialog.center();
    }

}
