package org.activityinfo.model.type;

import com.google.gson.JsonObject;

/**
 * A {@code FieldType} with parameters that further specialize
 * the type class.
 */
public interface ParametrizedFieldType extends FieldType {

    JsonObject getParametersAsJson();
    
    /**
     *
     * @return true if this is a valid type, false if its parameters make it invalid
     */
    boolean isValid();
}
