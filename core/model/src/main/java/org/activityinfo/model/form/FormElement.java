package org.activityinfo.model.form;

import com.google.gson.JsonElement;
import org.activityinfo.model.resource.ResourceId;

public abstract class FormElement {

    public abstract ResourceId getId();

    public abstract String getLabel();

    public abstract JsonElement toJsonObject();
}
