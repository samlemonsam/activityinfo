package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Random;

public class DateGenerator implements Supplier<FieldValue> {

    private double missingProbability;
    private final Random random;

    public DateGenerator(FormField field) {
        this.missingProbability = field.isRequired() ? 0.0 : 0.25;
        this.random = new Random(67680L);
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < missingProbability) {
            return null;
        }
        int year = 1995 + random.nextInt(50);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);

        return new LocalDate(year, month, day);
    }
}
