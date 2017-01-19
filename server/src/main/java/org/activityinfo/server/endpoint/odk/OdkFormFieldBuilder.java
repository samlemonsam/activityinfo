package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;

public interface OdkFormFieldBuilder {

    BindingType getModelBindType();

    BodyElement createBodyElement(String ref, String label, String hint);
}
