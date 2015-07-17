package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resources;

/**
 *
 */
public class FormClassResult implements CommandResult {
    private String json;

    public FormClassResult() {
    }

    public FormClassResult(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public FormClass getFormClass() {
        return FormClass.fromResource(Resources.fromJson(json));
    }
}
