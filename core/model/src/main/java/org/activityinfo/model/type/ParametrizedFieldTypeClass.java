package org.activityinfo.model.type;


import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.Record;

public interface ParametrizedFieldTypeClass extends FieldTypeClass {


    /**
     * Creates a parametrized FieldType using the parameters
     * specified in the provided {@code Record}
     * @param parameters a {@code Record} containing the type's parameters
     * @return an instance of {@code FieldType}
     */
    FieldType deserializeType(Record parameters);

    /**
     *
     * @return a FormClass that describes the FieldType's parameters
     */
    FormClass getParameterFormClass();

}
