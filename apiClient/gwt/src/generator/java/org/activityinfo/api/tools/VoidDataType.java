package org.activityinfo.api.tools;

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
    public String fromJsonString(String jsonStringExpr) {
        throw new UnsupportedOperationException();
    }
}
