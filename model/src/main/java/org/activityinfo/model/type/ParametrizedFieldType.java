package org.activityinfo.model.type;

import org.activityinfo.json.JsonValue;

/**
 * A {@code FieldType} with parameters that further specialize
 * the type class.
 */
public interface ParametrizedFieldType extends FieldType {

    JsonValue getParametersAsJson();
    
    /**
     *
     * @return true if this is a valid type, false if its parameters make it invalid
     */
    boolean isValid();
}
