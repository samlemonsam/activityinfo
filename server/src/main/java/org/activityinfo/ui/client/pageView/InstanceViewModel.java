package org.activityinfo.ui.client.pageView;

import org.activityinfo.core.shared.form.FormInstance;
import org.activityinfo.ui.client.page.instance.InstancePlace;

public class InstanceViewModel {
    private FormInstance instance;
    private String path;

    public InstanceViewModel(FormInstance instance, String path) {
        this.instance = instance;
        this.path = path != null ? path : InstancePlace.DEFAULT_VIEW;
    }

    public FormInstance getInstance() {
        return instance;
    }

    public String getPath() {
        return path;
    }
}
