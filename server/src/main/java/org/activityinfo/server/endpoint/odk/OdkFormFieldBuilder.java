package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;

public interface OdkFormFieldBuilder {

    final OdkFormFieldBuilder NONE = new OdkFormFieldBuilder() {
        @Override
        public BindingType getModelBindType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> getConstraint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BodyElement createBodyElement(String ref, String label, String hint) {
            throw new UnsupportedOperationException();
        }
    };

    BindingType getModelBindType();

    Optional<String> getConstraint();

    BodyElement createBodyElement(String ref, String label, String hint);
}
