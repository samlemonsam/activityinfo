package org.activityinfo.model.formTree;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.HasStringValue;

import java.util.Collections;
import java.util.List;

public class LookupKey {
    private final FormClass formSchema;
    private final String keyLabel;
    private final Optional<FormField> field;
    private final List<LookupKey> parentKeys;

    public LookupKey(String keyLabel, FormClass formSchema, FormField field, List<LookupKey> parentKeys) {
        this.formSchema = formSchema;
        this.field = Optional.of(field);
        this.keyLabel = keyLabel;
        this.parentKeys = Lists.newArrayList(parentKeys);
    }

    public LookupKey(FormClass formSchema, List<LookupKey> parentKeys) {
        this.formSchema = formSchema;
        this.field = Optional.absent();
        this.keyLabel = formSchema.getLabel();
        this.parentKeys = Lists.newArrayList(parentKeys);
    }

    public LookupKey(String keyLabel, FormClass formSchema, FormField field) {
        this(keyLabel, formSchema, field, Collections.<LookupKey>emptyList());
    }

    public ResourceId getFormId() {
        return formSchema.getId();
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


    public ExprNode getLabelFormula() {
        if(!field.isPresent()) {
            return new SymbolExpr(ColumnModel.ID_SYMBOL);

        } else {
            return new SymbolExpr(field.get().getId());
        }

    }

    public String getKeyLabel() {
        return keyLabel;
    }

    public boolean isRoot() {
        return parentKeys.isEmpty();
    }
}
