package org.activityinfo.api.tools;

import com.google.gson.JsonPrimitive;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;


public class PrimitiveDataType extends DataType {

    private TypeName typeName;

    public PrimitiveDataType(TypeName typeName) {
        this.typeName = typeName;
    }

    @Override
    public TypeName getReturnTypeName() {
        return typeName;
    }

    @Override
    public TypeName getParameterType() {
        return typeName;
    }

    @Override
    public String toJsonString(String valueExpr) {
        return valueExpr;
    }

    @Override
    public CodeBlock toJsonElement(String propertyExpr) {
        return CodeBlock.of("new $T($L)", JsonPrimitive.class, propertyExpr);
    }

    @Override
    public String fromJsonString(String jsonStringExpr) {
        throw new UnsupportedOperationException();
    }
}
