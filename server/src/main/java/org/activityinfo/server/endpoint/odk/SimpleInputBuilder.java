package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.Input;

class SimpleInputBuilder implements OdkFormFieldBuilder {
    final private BindingType modelBindType;
    private Optional<String> constraint;

    SimpleInputBuilder(BindingType modelBindType, Optional<String> constraint) {
        this.modelBindType = modelBindType;
        this.constraint = constraint;
    }

    SimpleInputBuilder(BindingType modelBindType) {
        this(modelBindType, Optional.<String>absent());
    }

    @Override
    public BindingType getModelBindType() {
        return modelBindType;
    }

    @Override
    public Optional<String> getConstraint() {
        return constraint;
    }

    @Override
    public Input createBodyElement(String ref, String label, String hint) {
        Input input = new Input();

        input.setRef(ref);
        input.setLabel(label);
        input.setHint(hint);

        return input;
    }
}
