package org.activityinfo.api.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.swagger.models.properties.Property;

/**
 * Template wrapper for a property of a model
 */
public class PropertyModel {

    private final String name;
    private final Property value;
    private final DefinitionModel schema;

    public PropertyModel(String name, Property value) {
        this.name = name;
        this.value = value;
        this.schema = null;
    }

    public PropertyModel(String name, Property model, DefinitionModel definitionModel) {
        Preconditions.checkNotNull(definitionModel, "%s has null definitionModel", name);
        this.name = name;
        this.value = model;
        this.schema = definitionModel;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return value.getRequired();
    }

    public String getRequiredString() {
        return value.getRequired() ? "required" : "optional";
    }

    public String getDescription() {
        return Strings.nullToEmpty(value.getDescription());
    }

    public String getType() {
        return value.getType();
    }

    public boolean isArray() {
        return "array".equals(value.getType());
    }



}
