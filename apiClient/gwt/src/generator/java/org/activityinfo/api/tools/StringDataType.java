package org.activityinfo.api.tools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;


public class StringDataType extends DataType {
    @Override
    public TypeName getReturnTypeName() {
        return ClassName.get(String.class);
    }

    @Override
    public TypeName getParameterType() {
        return ClassName.get(String.class);
    }

    @Override
    public String toJsonString(String valueExpr) {
        return valueExpr;
    }

    @Override
    public String fromJsonString(String jsonStringExpr) {
        throw new UnsupportedOperationException();
    }
}
