package org.activityinfo.api.tools;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Template-friendly wrapper for the Swagger specification object
 */
public class SpecModel {
    
    private Swagger spec;
    private List<ApiSectionModel> sections = new ArrayList<>();
    
    public SpecModel(Swagger spec) {
        this.spec = spec;

        List<OperationModel> operations = new ArrayList<>();
        for (Map.Entry<String, Path> path : spec.getPaths().entrySet()) {
            for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {
                operations.add(new OperationModel(path.getKey(),
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
