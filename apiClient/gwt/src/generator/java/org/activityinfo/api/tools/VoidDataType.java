package org.activityinfo.api.tools;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class VoidDataType extends DataType {
    @Override
    public TypeName getReturnTypeName() {
        return TypeName.VOID;
    }

    @Override
    public TypeName getParameterType() {
        return TypeName.VOID;
    }

    @Override
    public String toJsonString(String valueExpr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock toJsonElement(String propertyExpr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock fromJsonString(CodeBlock jsonStringExpr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock fromJsonElement(CodeBlock jsonElementExpr) {
        throw new UnsupportedOperationException();
    }
}
