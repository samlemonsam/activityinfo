package org.activityinfo.api.tools;

import com.google.gson.JsonParser;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.swagger.models.Model;

public class ModelDataType extends DataType {
    
    public static final String MODEL_PACKAGE = "org.activityinfo.api.client";

    private String name;
    private Model model;

    public ModelDataType(String name, Model model) {
        this.name = name;
        this.model = model;
        if(name == null) {
            throw new NullPointerException("name");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public TypeName getReturnTypeName() {
       return ClassName.get(MODEL_PACKAGE, name);
    }

    @Override
    public TypeName getParameterType() {
        return ClassName.get(MODEL_PACKAGE, name + "Builder");
    }

    @Override
    public String toJsonString(String valueExpr) {
        return valueExpr + ".toJsonString()";
    }

    @Override
    public CodeBlock toJsonElement(String propertyExpr) {
        return CodeBlock.of(propertyExpr + ".toJsonObject()");
    }

    @Override
    public CodeBlock fromJsonString(CodeBlock jsonStringExpr) {
        return CodeBlock.of("$T.fromJson(new $T().parse($L))", 
            getReturnTypeName(), 
            JsonParser.class,
            jsonStringExpr);
    }

    @Override
    public CodeBlock fromJsonElement(CodeBlock jsonElementExpr) {
        return CodeBlock.of("$T.fromJson($L)", 
            getReturnTypeName(), 
            jsonElementExpr);
    }

    @Override
    public CodeBlock fromJsonArray(CodeBlock jsonArrayExpr) {
        return CodeBlock.of("$T.fromJsonArray($L)",
                getReturnTypeName(),
                jsonArrayExpr);
    }
}
