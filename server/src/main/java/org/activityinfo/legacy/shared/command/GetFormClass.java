package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.FormClassResult;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;

/**
 * Retrieves the FormClass definition
 */
public class GetFormClass implements Command<FormClassResult> {

    private String resourceId;

    public GetFormClass() {
    }

    public GetFormClass(ResourceId resourceId) {
        this.resourceId = resourceId.asString();
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetFormClass that = (GetFormClass) o;

        if (!resourceId.equals(that.resourceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }
}
