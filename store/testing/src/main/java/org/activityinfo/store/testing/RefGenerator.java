package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.Random;

public class RefGenerator implements Supplier<FieldValue> {

    private final Random random = new Random(92412451L);
    private final TestForm rangeForm;


    public RefGenerator(TestForm rangeForm) {
        this.rangeForm = rangeForm;
    }

    @Override
    public FieldValue get() {
        int index = random.nextInt(rangeForm.getRecords().size());
        RecordRef recordRef = rangeForm.getRecords().get(index).getRef();
        return new ReferenceValue(recordRef);
    }
}
