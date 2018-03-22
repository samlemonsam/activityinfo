package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.activityinfo.json.Json;

import javax.lang.model.type.PrimitiveType;

public class PrimitiveMetaType implements MetaType {
    private final PrimitiveType type;

    public PrimitiveMetaType(PrimitiveType type) {
        this.type = type;
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock valueBlock) {
        return valueBlock;
    }

    @Override
    public CodeBlock toJsonArray(CodeBlock value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CodeBlock getJsonSerializerMethodRef() {
        return CodeBlock.of("$T::create", Json.class);
    }

    @Override
    public TypeName getTypeName() {
        return TypeName.get(type);
    }

    @Override
    public CodeBlock fromJsonProperty(CodeBlock jsonValue, String name) {
        if(getTypeName().equals(TypeName.BOOLEAN)) {
            return CodeBlock.of("$L.getBoolean($S)", jsonValue, name);
        } else {
            return CodeBlock.of("($T)$L.getNumber($S)", getTypeName(), jsonValue, name);
        }
    }

    @Override
    public CodeBlock fromJsonValue(CodeBlock jsonValue) {
        if(getTypeName().equals(TypeName.BOOLEAN)) {
            return CodeBlock.of("$L.asBoolean()", jsonValue);
        } else {
            return CodeBlock.of("($T)$L.asNumber()", getTypeName(), jsonValue);
        }
    }
}
