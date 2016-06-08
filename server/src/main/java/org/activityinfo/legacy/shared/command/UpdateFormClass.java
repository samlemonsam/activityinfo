package org.activityinfo.legacy.shared.command;


import com.google.common.base.Preconditions;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.resource.Resources;

import java.util.List;

public class UpdateFormClass implements Command<VoidResult> {

    private int databaseId;
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

    public static BatchCommand batchCommandForMultipleFormClasses(List<? extends IsResource> resources) {
        Preconditions.checkState(isAllFormClasses(resources));

        BatchCommand batchCommand = new BatchCommand();
        for (IsResource resource : resources) {
            batchCommand.add(new UpdateFormClass((FormClass) resource));
        }

        return batchCommand;
    }

    public static boolean isAllFormClasses(List<? extends IsResource> resources) {
        if (!resources.isEmpty()) {
            for (IsResource resource : resources) {
                if (!(resource instanceof FormClass)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UpdateFormClass{" + formClassId + "}";
    }
}
