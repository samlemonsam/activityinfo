package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.Input;

class SimpleInputBuilder implements OdkFormFieldBuilder {
    final private BindingType modelBindType;

    SimpleInputBuilder(BindingType modelBindType) {
        this.modelBindType = modelBindType;
    }

    @Override
    public BindingType getModelBindType() {
        return modelBindType;
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
