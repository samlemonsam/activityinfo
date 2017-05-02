package org.activityinfo.ui.client.input.view;


import com.sencha.gxt.widget.core.client.Dialog;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.store.FormStore;

public class FormDialog {

    private ResourceId formId;
    private FormInputView formInputView;
    private final Dialog dialog;

    public FormDialog(FormStore formStore, ResourceId formId) {
        this.formId = formId;

        FormInputView panel = new FormInputView(formStore, formId);

        dialog = new Dialog();
        dialog.setHeading("Form");
        dialog.setPixelSize(640, 480);
        dialog.setModal(true);
        dialog.add(panel);
    }

    public void show() {
        dialog.show();
        dialog.center();
    }

}
