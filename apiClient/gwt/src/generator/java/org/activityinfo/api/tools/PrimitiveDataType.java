package org.activityinfo.api.tools;

import com.google.gson.JsonPrimitive;
import com.squareup.javapoet.ClassName;
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
    public CodeBlock fromJsonString(CodeBlock jsonStringExpr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodeBlock fromJsonElement(CodeBlock jsonElementExpr) {
        if(typeName.equals(TypeName.BOOLEAN)) {
            return CodeBlock.of("$L.getAsBoolean()", jsonElementExpr);
        } else if(typeName.equals(TypeName.INT)) {
            return CodeBlock.of("$L.getAsInt()", jsonElementExpr);
        } else if(typeName.equals(TypeName.DOUBLE)) {
            return CodeBlock.of("$L.getAsDouble()", jsonElementExpr);
        } else if(typeName.equals(ClassName.get(String.class))) {
            return CodeBlock.of("$L.getAsString()", jsonElementExpr);
        } else {
            throw new UnsupportedOperationException("TODO: " + jsonElementExpr);
        }
    }
}
