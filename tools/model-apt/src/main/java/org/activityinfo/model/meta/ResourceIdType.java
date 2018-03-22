package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.activityinfo.json.JsonArrays;

import javax.lang.model.element.TypeElement;

public class ResourceIdType implements MetaType {
    private final TypeElement typeElement;

    public ResourceIdType(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock value) {
        return CodeBlock.of("$L.asString()", value);
    }

    @Override
    public CodeBlock toJsonArray(CodeBlock value) {
        return CodeBlock.of("$T.toJsonArray($L, $L)",
                JsonArrays.class,
                value,
                getJsonSerializerMethodRef());

    }

    @Override
    public CodeBlock getJsonSerializerMethodRef() {
        return CodeBlock.of("$T::toJson", typeElement.asType());
    }

    @Override
    public TypeName getTypeName() {
        return TypeName.get(typeElement.asType());
    }

    @Override
    public CodeBlock fromJsonProperty(CodeBlock jsonValue, String name) {
        return CodeBlock.of("$T.valueOf($L.getString($S))", typeElement, jsonValue, name);
    }

    @Override
    public CodeBlock fromJsonValue(CodeBlock jsonValue) {
        throw new UnsupportedOperationException("TODO");
    }
}
