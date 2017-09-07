package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;

public class EmptyEnumGenerator implements Supplier<FieldValue> {

    public EmptyEnumGenerator() {
    }

    @Override
    public FieldValue get() {
        return null;
    }

}
