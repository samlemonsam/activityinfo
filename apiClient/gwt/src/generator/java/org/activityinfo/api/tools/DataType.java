package org.activityinfo.api.tools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.activityinfo.promise.Promise;

public abstract class DataType {
    
    
    public abstract TypeName getReturnTypeName();
    
    public TypeName getPromisedReturnType() {
        ClassName promise = ClassName.get(Promise.class);
        return ParameterizedTypeName.get(promise, getReturnTypeName().box());
    }

    public abstract TypeName getParameterType();

    /**
     * Returns a Java expression that evaluates to the JSON String representation of 
     * {@code valueExpr}
     */
    public abstract String toJsonString(String valueExpr);

    /**
     * Returns a Java expression that parses the Java string expression
     */
    public abstract String fromJsonString(String jsonStringExpr);
}
