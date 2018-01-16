package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RefGenerator implements Supplier<FieldValue> {

    private final Random random = new Random(92412451L);
    private final List<TestForm> rangeForms;


    public RefGenerator(TestForm... rangeForms) {
        this.rangeForms = Arrays.asList(rangeForms);
    }

    public RefGenerator(Iterable<TestForm> rangeForms) {
        this.rangeForms = Lists.newArrayList(rangeForms);
    }

    @Override
    public FieldValue get() {

        TestForm rangeForm;
        if(rangeForms.size() == 1) {
            rangeForm = rangeForms.get(0);
        } else {
            rangeForm = rangeForms.get(random.nextInt(rangeForms.size()));
        }

        int index = random.nextInt(rangeForm.getRecords().size());
        RecordRef recordRef = rangeForm.getRecords().get(index).getRef();
        return new ReferenceValue(recordRef);
    }
}
