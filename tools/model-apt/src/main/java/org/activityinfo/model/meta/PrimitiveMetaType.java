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
}
