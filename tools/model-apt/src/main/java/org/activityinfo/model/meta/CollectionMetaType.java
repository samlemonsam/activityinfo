package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class CollectionMetaType implements MetaType {

    private final DeclaredType collectionType;
    private final MetaType elementType;

    public CollectionMetaType(DeclaredType collectionType, TypeMirror elementType) {
        this.collectionType = collectionType;
        this.elementType = MetaTypes.of(elementType);
    }

    @Override
    public TypeName getTypeName() {
        return TypeName.get(collectionType);
    }

    public MetaType getElementType() {
        return elementType;
    }

    @Override
    public CodeBlock toJsonPuttable(CodeBlock value) {
        return elementType.toJsonArray(value);
    }

    @Override
    public CodeBlock toJsonArray(CodeBlock value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock getJsonSerializerMethodRef() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CodeBlock fromJsonProperty(CodeBlock jsonValue, String name) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CodeBlock fromJsonValue(CodeBlock jsonValue) {
        throw new UnsupportedOperationException("TODO");
    }
}
