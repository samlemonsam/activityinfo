package org.activityinfo.store.query.shared;

import com.google.common.base.Function;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;

class EnumItemReader implements Function<FieldValue, FieldValue> {

    private ResourceId itemId;

    public EnumItemReader(ResourceId itemId) {
        this.itemId = itemId;
    }

    @Override
    public FieldValue apply(FieldValue input) {
        if(input instanceof EnumValue) {
            EnumValue value = (EnumValue) input;
            if(value.getResourceIds().contains(itemId)) {
                return BooleanFieldValue.TRUE;
            }
        }
        return BooleanFieldValue.FALSE;
    }
}
