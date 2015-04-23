package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Input;

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
