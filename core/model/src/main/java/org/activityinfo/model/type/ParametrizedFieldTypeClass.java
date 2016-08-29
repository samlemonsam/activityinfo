package org.activityinfo.model.type;


import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;

public interface ParametrizedFieldTypeClass extends FieldTypeClass {


    /**
     * Creates a parametrized FieldType using the parameters
     * specified in the provided {@code Record}
     * @param parameters a {@code Record} containing the type's parameters
     * @return an instance of {@code FieldType}
     */
    @Deprecated
    FieldType deserializeType(Record parameters);

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
