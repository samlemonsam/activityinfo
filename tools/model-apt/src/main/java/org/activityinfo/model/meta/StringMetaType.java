package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.activityinfo.json.Json;

import javax.lang.model.element.TypeElement;

public class StringMetaType implements MetaType {
    private final TypeElement typeElement;

    public StringMetaType(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock value) {
        return value;
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
        return TypeName.get(typeElement.asType());
    }
}
