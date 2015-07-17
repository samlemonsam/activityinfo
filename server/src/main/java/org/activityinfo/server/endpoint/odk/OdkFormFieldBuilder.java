package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;

public interface OdkFormFieldBuilder {

    public BindingType getModelBindType();

    public BodyElement createBodyElement(String ref, String label, String hint);
}
