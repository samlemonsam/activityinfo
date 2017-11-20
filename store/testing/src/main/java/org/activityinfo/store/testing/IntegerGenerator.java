package org.activityinfo.store.testing;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;

import java.util.Random;

public class IntegerGenerator implements Supplier<FieldValue> {
    private int minValue;
    private int range;
    private final Random random;
    private double probabilityMissing;

    public IntegerGenerator(int min, int max, double probabilityMissing) {
        Preconditions.checkArgument(max > min, "max must be greater than min");
        this.minValue = min;
        this.range = (max - min);
        this.random = new Random(432222L);
        this.probabilityMissing = probabilityMissing;
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        } else {
            double quantity = minValue + random.nextInt(range);
            return new Quantity(quantity);
        }
    }
}
