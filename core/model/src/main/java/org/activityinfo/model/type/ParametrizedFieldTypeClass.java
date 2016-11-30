package org.activityinfo.model.type;


import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;

public interface ParametrizedFieldTypeClass extends FieldTypeClass {


    /**
     * Creates a parametrized FieldType using the parameters
     * specified in the provided {@code JsonObject}
     * 
     * @return an instance of {@code FieldType}
     */
    FieldType deserializeType(JsonObject parametersObject);

    /**
     *
     * @return a FormClass that describes the FieldType's parameters
     */
    FormClass getParameterFormClass();

}
