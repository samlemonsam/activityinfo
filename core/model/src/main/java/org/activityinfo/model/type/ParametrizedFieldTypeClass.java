package org.activityinfo.model.type;


import org.activityinfo.json.JsonValue;

public interface ParametrizedFieldTypeClass extends FieldTypeClass {


    /**
     * Creates a parametrized FieldType using the parameters
     * specified in the provided {@code JsonObject}
     * 
     * @return an instance of {@code FieldType}
     */
    FieldType deserializeType(JsonValue parametersObject);

}
