package org.activityinfo.legacy.shared.command;


import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Resources;

public class UpdateFormClass implements Command<VoidResult> {

    private String formClassId;
    private String json;

    public UpdateFormClass() {
    }

    public UpdateFormClass(FormClass formClass) {
        this.formClassId = formClass.getId().asString();
        this.json = Resources.toJson(formClass.asResource());
    }

    public String getFormClassId() {
        return formClassId;
    }

    public void setFormClassId(String formClassId) {
        this.formClassId = formClassId;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
