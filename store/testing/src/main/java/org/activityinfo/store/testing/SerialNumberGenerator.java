package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;

public class SerialNumberGenerator implements Supplier<FieldValue> {

    private int next = 1;


    @Override
    public FieldValue get() {
        return new SerialNumber(next ++);
    }
}
