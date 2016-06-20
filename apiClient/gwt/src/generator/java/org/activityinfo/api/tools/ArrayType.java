package org.activityinfo.api.tools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

public class ArrayType extends DataType {
    
    private DataType baseType;

    public ArrayType(DataType baseType) {
        this.baseType = baseType;
    }

    @Override
    public TypeName getReturnTypeName() {
        ClassName list = ClassName.get(List.class);
        return ParameterizedTypeName.get(list, baseType.getReturnTypeName());
    }

    @Override
    public TypeName getParameterType() {
        return getReturnTypeName();
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
