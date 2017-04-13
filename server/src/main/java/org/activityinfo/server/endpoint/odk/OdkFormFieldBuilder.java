package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;

public interface OdkFormFieldBuilder {

    final OdkFormFieldBuilder NONE = new OdkFormFieldBuilder() {
        @Override
        public BindingType getModelBindType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BodyElement createBodyElement(String ref, String label, String hint) {
            throw new UnsupportedOperationException();
        }
    };

    BindingType getModelBindType();

    BodyElement createBodyElement(String ref, String label, String hint);
}
