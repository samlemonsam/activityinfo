package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;

public class ModelMetaType implements MetaType {

    private final DeclaredType declaredType;
    private final ModelSerializationStrategy serializationStrategy;

    public ModelMetaType(DeclaredType declaredType) {
        this.declaredType = declaredType;
        this.serializationStrategy = ModelSerializationStrategy.of(declaredType);
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock value) {
        return serializationStrategy.toJsonValue(getTypeName(), value);
    }

    @Override
    public CodeBlock toJsonArray(CodeBlock value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CodeBlock getJsonSerializerMethodRef() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public TypeName getTypeName() {
        return TypeName.get(declaredType);
    }

    @Override
    public CodeBlock fromJsonProperty(CodeBlock jsonValue, String name) {
        return serializationStrategy.fromJson(getTypeName(), CodeBlock.of("$L.get($S)", jsonValue, name));
    }

    @Override
    public CodeBlock fromJsonValue(CodeBlock jsonValue) {
        return serializationStrategy.fromJson(getTypeName(), jsonValue);
    }
}
