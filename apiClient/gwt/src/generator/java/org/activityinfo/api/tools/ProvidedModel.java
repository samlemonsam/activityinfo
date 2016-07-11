package org.activityinfo.api.tools;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;

import java.util.HashMap;
import java.util.Map;

public class ProvidedModel extends DataType {
    
    private static final Map<String, ClassName> MODELS = new HashMap<>();
    
    static {
        MODELS.put("FormSchema", ClassName.get(FormClass.class));
        MODELS.put("FormRecord", ClassName.get(FormRecord.class));
        MODELS.put("ColumnSet", ClassName.get(ColumnSet.class));
        MODELS.put("TableQuery", ClassName.get(QueryModel.class));
    }

    public static boolean isProvided(String name) {
        return MODELS.containsKey(name);
    }


    private final String name;

    public ProvidedModel(String name) {
        this.name = name;
    }

    @Override
    public TypeName getReturnTypeName() {
        return MODELS.get(name);
    }

    @Override
    public TypeName getParameterType() {
        return MODELS.get(name);
    }

    @Override
    public String toJsonString(String valueExpr) {
        return valueExpr + ".toJsonString()";
    }

    @Override
    public CodeBlock toJsonElement(String propertyExpr) {
        return CodeBlock.of(propertyExpr + ".toJsonElement()");
    }

    @Override
    public CodeBlock fromJsonString(CodeBlock jsonStringExpr) {
        if(name.equals("ColumnSet")) {
            return CodeBlock.of("ColumnSetParser.fromJson($L)", jsonStringExpr); 
        } else {
            return CodeBlock.of("$T.fromJson($L)", MODELS.get(name), jsonStringExpr);
        }
    }

    @Override
    public CodeBlock fromJsonElement(CodeBlock jsonElementExpr) {
        return fromJsonString(jsonElementExpr);
    }

    @Override
    public CodeBlock fromJsonArray(CodeBlock jsonArrayExpr) {
        return CodeBlock.of("$T.fromJsonArray($L)", MODELS.get(name), jsonArrayExpr);
    }

    @Override
    public String toString() {
        return "ProvidedModel{" + name + "}";
    }
}
