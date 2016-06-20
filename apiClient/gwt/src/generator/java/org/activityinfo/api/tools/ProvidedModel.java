package org.activityinfo.api.tools;


import com.squareup.javapoet.ClassName;
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
    public String fromJsonString(String jsonStringExpr) {
        if(name.equals("ColumnSet")) {
            return "ColumnSetParser.fromJson(" + jsonStringExpr + ")";
        } else {
            return MODELS.get(name).simpleName() + ".fromJson(" + jsonStringExpr + ")";
        }
    }
}
