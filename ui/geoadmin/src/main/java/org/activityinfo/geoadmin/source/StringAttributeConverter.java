package org.activityinfo.geoadmin.source;

import com.google.common.base.Function;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import javax.annotation.Nullable;


public class StringAttributeConverter implements Function<Object, FieldValue> {
    @Nullable
    @Override
    public FieldValue apply(Object input) {
        assert input instanceof String;
        String string = (String)input;
        return TextValue.valueOf(string);
    }
}
