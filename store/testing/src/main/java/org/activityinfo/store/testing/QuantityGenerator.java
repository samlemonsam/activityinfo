package org.activityinfo.store.testing;


import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

import java.util.Random;


public class QuantityGenerator implements Supplier<FieldValue> {

    private double minValue;
    private double maxValue;
    private double probabilityMissing;
    private final Random random;
    private String units;

    public QuantityGenerator(FormField field) {
        this.minValue = -15;
        this.maxValue = 100;
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.25;
        this.random = new Random(135L);
        this.units = ((QuantityType) field.getType()).getUnits();
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double quantity = minValue + (random.nextDouble() * (maxValue - minValue));
            return new Quantity(quantity, units);
        }
    }
}
