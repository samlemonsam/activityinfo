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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Template-friendly wrapper for the Swagger specification object
 */
public class SpecModel {
    
    private Swagger spec;
    private List<ApiSectionModel> sections = new ArrayList<>();
    private Map<String, DefinitionModel> definitions = new HashMap<>();
    
    public SpecModel(Swagger spec) {
        this.spec = spec;

        for (Map.Entry<String, Model> entry : spec.getDefinitions().entrySet()) {
            definitions.put(entry.getKey(), new DefinitionModel(entry.getKey(), entry.getValue()));
        }

        for (DefinitionModel definitionModel : definitions.values()) {
            definitionModel.build(definitions);
        }

        List<OperationModel> operations = new ArrayList<>();
        for (Map.Entry<String, Path> path : spec.getPaths().entrySet()) {
            for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {
                operations.add(new OperationModel(definitions, path.getKey(),
                        operation.getKey(), operation.getValue()));
            }
        }

        sections.add(new ApiSectionModel("forms", "Forms API", operations));
        sections.add(new ApiSectionModel("records", "Records API", operations));
        sections.add(new ApiSectionModel("query", "Query API", operations));
    }

    public String getBaseUri() {
        return "https://www.activityinfo.org/resources";
    }
    
    public List<ApiSectionModel> getSections() {
        return sections;
    }

}

