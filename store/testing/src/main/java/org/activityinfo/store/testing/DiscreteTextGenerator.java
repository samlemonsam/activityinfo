package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.Arrays;
import java.util.Random;

public class DiscreteTextGenerator implements Supplier<FieldValue> {

    public static final String[] NAMES = new String[] { "Bob", "George", "Joe", "Melanie", "Sue", "Franz", "Jane", "Matilda" };


    private final String[] values;
    private final Random random;
    private double probabilityMissing;

    public DiscreteTextGenerator(double probabilityMissing, String... values) {
        this.probabilityMissing = probabilityMissing;
        this.values = Arrays.copyOf(values, values.length);
        this.random = new Random(356432L);
    }

    @Override
    public FieldValue get() {

        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        }

        int valueIndex = random.nextInt(values.length);
        String value = values[valueIndex];
        return TextValue.valueOf(value);
    }
}
