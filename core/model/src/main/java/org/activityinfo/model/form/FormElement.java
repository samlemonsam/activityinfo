package org.activityinfo.model.form;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

public abstract class FormElement {

    public abstract ResourceId getId();

    public abstract String getLabel();

    public abstract JsonValue toJsonObject();
}
