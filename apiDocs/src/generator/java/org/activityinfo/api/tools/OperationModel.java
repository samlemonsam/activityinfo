package org.activityinfo.api.tools;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OperationModel {
    private final String uri;
    private final Path path;
    private final HttpMethod method;
    private final Operation operation;
    
    private String jsonOutput;
    private List<ExampleModel> examples = new ArrayList<>();

    public OperationModel(String uri, Path path, HttpMethod method, Operation operation) {
        this.uri = uri;
        this.path = path;
        this.method = method;
        this.operation = operation;
        
        this.jsonOutput = tryReadExample(".json");
        tryAddExample("shell", ".sh");
    }



    public String getId() {
        return operation.getOperationId();
    }

    public String getPath() {
        return uri;
    }

    public String getMethod() {
        return method.name();
    }

    
    public String getSummary() {
        return operation.getSummary();
    }

    public String getJsonOutput() {
        return jsonOutput;
    }

    public List<ExampleModel> getExamples() {
        return examples;
    }

    private void tryAddExample(String language, String extension) {
        String source = tryReadExample(extension);
        if(source != null) {
            
            
            ExampleModel model = new ExampleModel();
            model.setLanguage(language);
            model.setSource(source);
            examples.add(model);
        }
    }
    
    private String tryReadExample(String ext)  {
        File file = new File("apiDocs/src/main/examples/" + getId() + ext);
        if(!file.exists()) {
            return null;
        }
        try {
            String source = Files.toString(file, Charsets.UTF_8);
            source = source.replaceAll("@ROOT_URL@", "https://www.activityinfo.org");
            return source;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ParamModel> getParameters() {
        List<ParamModel> params = new ArrayList<>();
        for (Parameter parameter : operation.getParameters()) {
            if (!parameter.getIn().equals("body")) {
                params.add(new ParamModel(parameter));
            }
        }
        return params;
    }   
}
