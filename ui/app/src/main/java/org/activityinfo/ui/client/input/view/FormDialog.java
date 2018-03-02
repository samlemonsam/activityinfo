package org.activityinfo.ui.client.input.view;


import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.store.FormStore;

public class FormDialog {

    private ResourceId formId;
    private FormInputView formInputView;
    private final Dialog dialog;

    public FormDialog(FormStore formStore, RecordRef recordRef) {
        this.formId = formId;

        FormInputView panel = new FormInputView(formStore, recordRef);

        dialog = new Dialog();
        dialog.setHeading(I18N.CONSTANTS.form());
        dialog.setPixelSize(640, 480);
        dialog.setModal(true);
        dialog.setWidget(panel);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                panel.save(closeEvent -> {
                   dialog.hide();
                });
            }
        });
    }

    public void show() {
        dialog.show();
        dialog.center();
    }

}
