package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

class UniqueNameGenerator implements Supplier<FieldValue> {

    private int index = 1;
    private String prefix;

    UniqueNameGenerator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public FieldValue get() {
        return TextValue.valueOf(prefix + " " + (index++));
    }
}
