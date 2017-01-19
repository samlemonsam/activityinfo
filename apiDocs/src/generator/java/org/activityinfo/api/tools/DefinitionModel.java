package org.activityinfo.api.tools;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Template-friendly wrapper for an OpenAPI model
 */
public class DefinitionModel {

    private String name;
    private Model value;

    private List<PropertyModel> properties = new ArrayList<>();

    public DefinitionModel(String name, Model value) {
        this.name = name;
        this.value = value;

    }

    public void build(Map<String, DefinitionModel> definitions) {
        for (Map.Entry<String, Property> entry : value.getProperties().entrySet()) {
            properties.add(build(definitions, entry.getKey(), entry.getValue()));
        }

    }

    private PropertyModel build(Map<String, DefinitionModel> definitions, String name, Property model) {
        if(model instanceof RefProperty) {
            return new PropertyModel(name, model, definitions.get(((RefProperty) model).getSimpleRef()));
        } else {
            return new PropertyModel(name, model);
        }
    }


    public String getName() {
        return name;
    }

    public List<PropertyModel> getProperties() {
        return properties;
    }
}
