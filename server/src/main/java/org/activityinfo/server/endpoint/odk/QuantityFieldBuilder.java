package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Optional;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Input;
import org.activityinfo.model.type.number.QuantityType;

class QuantityFieldBuilder implements OdkFormFieldBuilder {
    final private String units;

    QuantityFieldBuilder(QuantityType quantityType) {
        this.units = quantityType.getUnits();
    }

    @Override
    public BindingType getModelBindType() {
        return BindingType.DECIMAL;
    }

    @Override
    public Optional<String> getConstraint() {
        return Optional.absent();
    }

    @Override
    public BodyElement createBodyElement(String ref, String label, String hint) {
        Input input = new Input();
        input.setRef(ref);

        if (units == null) {
            input.setLabel(label);
        } else if (label == null) {
            input.setLabel(units);
        } else {
            input.setLabel(label + " [" + units + ']');
        }
        input.setHint(hint);

        return input;
    }
}
