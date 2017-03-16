package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Random;

public class DateGenerator implements Supplier<FieldValue> {

    private double missingProbability;
    private final Random random;
    private int minYear;
    private int maxYear;

    public DateGenerator(FormField field, int minYear, int maxYear) {
        this.missingProbability = field.isRequired() ? 0.0 : 0.25;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.random = new Random(67680L);
    }

    public DateGenerator(FormField field) {
        this(field, 1995, 1995+50);
    }

    @Override
    public FieldValue get() {
        double missing = random.nextDouble();
        if(missing < missingProbability) {
            return null;
        }
        int year = minYear + random.nextInt(maxYear - minYear);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);

        return new LocalDate(year, month, day);
    }
}
