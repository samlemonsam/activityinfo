package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MultiEnumGenerator implements Supplier<FieldValue> {

    private EnumType enumType;
    private double probabilityMissing;
    private final double[] probabilities;
    private Random random;

    public MultiEnumGenerator(FormField field, double... probabilities) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.10;
        this.probabilities = Arrays.copyOf(probabilities, probabilities.length);
        this.random = new Random(field.getId().hashCode());
        assert probabilities.length == enumType.getValues().size();
    }

    public MultiEnumGenerator(FormField field) {
        this.enumType = (EnumType) field.getType();
        this.probabilityMissing = field.isRequired() ? 0.0 : 0.10;
        this.probabilities = new double[enumType.getValues().size()];
        Arrays.fill(probabilities, 0.15);

        this.random = new Random(field.getId().hashCode());
        assert probabilities.length == enumType.getValues().size();
    }

    @Override
    public FieldValue get() {
        if(random.nextDouble() < probabilityMissing) {
            return null;
        }
        List<ResourceId> ids = new ArrayList<>();
        for (int i = 0; i < probabilities.length; i++) {
            if(random.nextDouble() < probabilities[i]) {
                ids.add(enumType.getValues().get(i).getId());
            }
        }
        return new EnumValue(ids);
    }
}
