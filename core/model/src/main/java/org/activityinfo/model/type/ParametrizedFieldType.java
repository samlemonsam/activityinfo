package org.activityinfo.model.type;

/**
 * A {@code FieldType} with parameters that further specialize
 * the type class.
 */
public interface ParametrizedFieldType extends FieldType {

    org.activityinfo.json.JsonObject getParametersAsJson();
    
    /**
     *
     * @return true if this is a valid type, false if its parameters make it invalid
     */
    boolean isValid();
}
