package org.activityinfo.model.formTree;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.HasStringValue;

import java.util.Collections;
import java.util.List;

public class LookupKey {
    private final FormClass formSchema;
    private final Optional<FormField> field;
    private final List<LookupKey> parentKeys;

    public LookupKey(FormClass formSchema, FormField field, List<LookupKey> parentKeys) {
        this.formSchema = formSchema;
        this.field = Optional.of(field);
        this.parentKeys = Lists.newArrayList(parentKeys);
    }

    public LookupKey(FormClass formSchema, List<LookupKey> parentKeys) {
        this.formSchema = formSchema;
        this.field = Optional.absent();
        this.parentKeys = Lists.newArrayList(parentKeys);
    }

    public LookupKey(FormClass formSchema, FormField field) {
        this.formSchema = formSchema;
        this.field = Optional.of(field);
        this.parentKeys = Collections.emptyList();
    }


    public String label(FormInstance record) {
        if(!field.isPresent()) {
            return record.getId().asString();
        }

        FieldValue fieldValue = record.get(field.get().getId());
        if(fieldValue == null) {
            return record.getId().asString();
        }

        if(field.get().getType() instanceof SerialNumberType) {
            SerialNumberType type = (SerialNumberType) field.get().getType();
            return type.format(((SerialNumber) fieldValue));

        } else {
            return ((HasStringValue) fieldValue).asString();
        }
    }
}
