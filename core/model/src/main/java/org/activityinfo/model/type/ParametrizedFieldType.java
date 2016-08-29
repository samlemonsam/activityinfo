package org.activityinfo.model.type;

import com.google.gson.JsonObject;
import org.activityinfo.model.resource.Record;

/**
 * A {@code FieldType} with parameters that further specialize
 * the type class.
 */
public interface ParametrizedFieldType extends FieldType {

    /**
     *
     * @return a {@code Record} containing this type's parameters.
     */
    Record getParameters();

    JsonObject getParametersAsJson();
    
    /**
     *
     * @return true if this is a valid type, false if its parameters make it invalid
     */
    boolean isValid();
}
