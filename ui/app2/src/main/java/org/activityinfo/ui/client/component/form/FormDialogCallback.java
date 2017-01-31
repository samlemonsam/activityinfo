package org.activityinfo.ui.client.component.form;


import org.activityinfo.model.form.FormInstance;

import java.util.List;

public interface FormDialogCallback {

    void onPersisted(List<FormInstance> instance);
}
