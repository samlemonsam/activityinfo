package org.activityinfo.api.tools;

import com.google.gson.JsonObject;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;


public class ObjectType extends DataType {
    @Override
    public TypeName getReturnTypeName() {
        return ClassName.get(JsonObject.class);
    }

    @Override
    public TypeName getParameterType() {
        return ClassName.get(JsonObject.class);
    }

    @Override
    public String toJsonString(String valueExpr) {
        return valueExpr + ".toString()";
    }

    @Override
    public CodeBlock toJsonElement(String propertyExpr) {
        return CodeBlock.of("$L", propertyExpr);
    }

    @Override
    public String fromJsonString(String jsonStringExpr) {
        throw new UnsupportedOperationException();
    }
}
