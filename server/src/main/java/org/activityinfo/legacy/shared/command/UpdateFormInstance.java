package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resources;

/**
 * Created by yuriy on 3/1/2015.
 */
public class UpdateFormInstance implements Command<VoidResult> {

    private String formInstanceId;
    private String json;

    public UpdateFormInstance() {
    }

    public UpdateFormInstance(FormInstance instance) {
        this.formInstanceId = instance.getId().asString();
        this.json = Resources.toJson(instance.asResource());
    }

    public UpdateFormInstance(String formInstanceId, String json) {
        this.formInstanceId = formInstanceId;
        this.json = json;
    }

    public String getFormInstanceId() {
        return formInstanceId;
    }

    public UpdateFormInstance setFormInstanceId(String formInstanceId) {
        this.formInstanceId = formInstanceId;
        return this;
    }

    public String getJson() {
        return json;
    }

    public UpdateFormInstance setJson(String json) {
        this.json = json;
        return this;
    }
}
