/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
