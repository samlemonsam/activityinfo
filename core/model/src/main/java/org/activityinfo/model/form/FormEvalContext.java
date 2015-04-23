package org.activityinfo.model.form;

import com.google.common.collect.Maps;
import org.activityinfo.model.expr.eval.*;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;

import java.util.Map;

public class FormEvalContext implements EvalContext {

    /**
     * Maps form ids and codes to a value source, either
     * a static field or calculated field
     */
    private final Map<String, ValueSource> symbolMap = Maps.newHashMap();

    private final Map<String, ValueSource> fieldMap = Maps.newHashMap();


    private Resource formInstance;

    public FormEvalContext(FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            ValueSource source = createValueSource(field);
            if (field.hasCode()) {
                symbolMap.put(field.getCode(), source);
            }
            symbolMap.put(field.getId().asString(), source);
            fieldMap.put(field.getId().asString(), source);
        }

        // TODO: cleanup hack: enum values need to be treated as constants, not symbols!

        for (FormField field : formClass.getFields()) {
            if (field.getType() instanceof EnumType) {
                for (EnumItem item : ((EnumType) field.getType()).getValues()) {
                    symbolMap.put(item.getId().asString(), new ConstantValue(new EnumValue(item.getId())));
                }
            }
        }
    }


    public FormEvalContext(FormClass formClass, Resource resource) {
        this(formClass);
        setInstance(resource);
    }

    public FormEvalContext(FormClass formClass, FormInstance instance) {
        this(formClass);
        setInstance(instance.asResource());
    }

    public void setInstance(Resource resource) {
        this.formInstance = resource;
    }

    public void setInstance(FormInstance instance) {
        setInstance(instance.asResource());
    }

    public ResourceId getId() {
        return formInstance.getId();
    }

    public FieldValue getFieldValue(String fieldName) {
        assert formInstance != null;
        try {
            return fieldMap.get(fieldName).getValue(formInstance, this);
        } catch(Exception e) {
            return new ErrorValue(e);
        }
    }

    public FieldValue getFieldValue(ResourceId fieldId) {
        return getFieldValue(fieldId.asString());
    }

    private ValueSource createValueSource(FormField field) {

        if (field.getType() instanceof CalculatedFieldType) {
            return new CalculatedField(field);
        } else {
            return new StaticField(field);
        }
    }

    public FieldType resolveFieldType(ResourceId fieldId) {
        return fieldMap.get(fieldId.asString()).resolveType(this);
    }

    @Override
    public FieldValue resolveSymbol(String symbolName) {
        return lookupSymbol(symbolName).getValue(formInstance, this);
    }

    @Override
    public FieldType resolveSymbolType(String name) {
        return lookupSymbol(name).resolveType(this);
    }

    private ValueSource lookupSymbol(String symbolName) {
        ValueSource valueSource = symbolMap.get(symbolName);
        if (valueSource == null) {
            // todo : we must fix it, here as temporary solution if symbol name can't be resolved we consider it as ReferenceValue
            return new ConstantValue(new ReferenceValue(ResourceId.valueOf(symbolName)));
//            throw new RuntimeException("Unknown symbol '" + symbolName + "'");
        }
        return valueSource;
    }
}
