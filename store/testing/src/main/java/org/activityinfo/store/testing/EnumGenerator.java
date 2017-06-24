package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.Random;

/**
 * Generates values for a single select field
 */
public class EnumGenerator implements Supplier<FieldValue> {

    private EnumType enumType;
    private double probabilityMissing;
    private Random random;

    public EnumGenerator(FormField field, int seed) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.25;
        this.random = new Random(seed);
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < probabilityMissing) {
            return null;
        }

        int itemIndex = random.nextInt(enumType.getValues().size());
        EnumItem item = enumType.getValues().get(itemIndex);

        return new EnumValue(item.getId());
    }
}
