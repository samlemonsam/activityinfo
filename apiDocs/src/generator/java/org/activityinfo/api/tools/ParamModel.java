package org.activityinfo.api.tools;

import io.swagger.models.parameters.Parameter;

/**
 * Template-friendly model of a request's parameter.
 */
public class ParamModel {


    private Parameter parameter;

    public ParamModel(Parameter parameter) {

        this.parameter = parameter;
    }
    
    public boolean isOptional() {
        return !parameter.getRequired();
    }
    
    public String getName() {
        return parameter.getName();
    }
    
    public String getDescription() {
        return parameter.getDescription();
    }
    
}
