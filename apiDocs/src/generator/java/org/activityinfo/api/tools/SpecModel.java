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

    public SpecModel(Swagger spec) {
        this.spec = spec;
    }
    
    public List<OperationModel> getOperations() {
        List<OperationModel> operations = new ArrayList<>();
        for (Map.Entry<String, Path> path : spec.getPaths().entrySet()) {
            for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {
                operations.add(new OperationModel(path.getKey(), path.getValue(), 
                        operation.getKey(), operation.getValue()));
            }
        }
        return operations;
    }
}
