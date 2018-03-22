package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.activityinfo.json.JsonArrays;

import javax.lang.model.element.TypeElement;

public class EnumMetaType implements MetaType {

    private final TypeElement element;

    public EnumMetaType(TypeElement element) {
        this.element = element;
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock value) {
        return CodeBlock.of("$L.name()", value);
    }

    @Override
    public CodeBlock toJsonArray(CodeBlock value) {
        return CodeBlock.of("$T.toJsonArrayFromEnums($L)", JsonArrays.class, value);
    }

    @Override
    public CodeBlock getJsonSerializerMethodRef() {
        return CodeBlock.of("$T::toJsonArrayFromEnums", JsonArrays.class);
    }

    @Override
    public TypeName getTypeName() {
        return TypeName.get(element.asType());
    }
}
