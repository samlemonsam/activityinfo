package org.activityinfo.api.tools;

import io.swagger.models.*;

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
        return "https://www.activityinfo.org";
    }
    
    public List<ApiSectionModel> getSections() {
        return sections;
    }

}

