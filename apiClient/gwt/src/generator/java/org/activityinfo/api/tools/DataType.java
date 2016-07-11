package org.activityinfo.api.tools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
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
     * Returns a Java expression that evalutes to a Gson JsonElement instance
     * @param propertyExpr
     */
    public abstract CodeBlock toJsonElement(String propertyExpr);

    /**
     * Returns a Java expression that parses the Java string expression
     * @param jsonStringExpr
     */
    public abstract CodeBlock fromJsonString(CodeBlock jsonStringExpr);

    public final CodeBlock fromJsonString(String jsonStringExpr) {
        return fromJsonString(CodeBlock.of(jsonStringExpr));
    }

    public abstract CodeBlock fromJsonElement(CodeBlock jsonElementExpr);
    
    public CodeBlock fromJsonArray(CodeBlock jsonArrayExpr) {
        throw new UnsupportedOperationException("TODO: this = " + this);
    }
}
