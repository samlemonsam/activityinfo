package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

import java.util.*;

/**
 * Generates references to another form, using each one at most once
 */
public class RefKeyGenerator implements Supplier<FieldValue> {


    private final Iterator<RecordRef> stream;

    public RefKeyGenerator(TestForm rangeForm) {
        List<RecordRef> range = new ArrayList<>();
        for (FormInstance record : rangeForm.getRecords()) {
            range.add(new RecordRef(rangeForm.getFormId(), record.getId()));
        }

        Collections.shuffle(range, new Random(19993345L));

        this.stream = range.iterator();
    }

    @Override
    public FieldValue get() {
        if(!stream.hasNext()) {
            throw new IllegalStateException("Cannot generate any more keys - all used");
        }
        return  new ReferenceValue(stream.next());
    }
}
