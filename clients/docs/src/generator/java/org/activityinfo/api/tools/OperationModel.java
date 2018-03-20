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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OperationModel {
    private final String uri;
    private final HttpMethod method;
    private final Operation operation;

    private DefinitionModel responseSchema = null;

    private String jsonOutput;
    private List<ExampleModel> examples = new ArrayList<>();
    private List<ResponseModel> responses = new ArrayList<>();
    
    private BodyParameter body = null;
    
    public OperationModel(Map<String, DefinitionModel> definitions, String uri, HttpMethod method, Operation operation) {
        this.uri = uri;
        this.method = method;
        this.operation = operation;
        
        this.jsonOutput = tryReadExample(".json");
        tryAddExample("shell", ".sh");

        ExampleModel curlExample = CurlExamplesGenerator.getExample(operation.getOperationId());
        if(curlExample != null) {
            examples.add(curlExample);
        }

        for (Parameter parameter : operation.getParameters()) {
            if(parameter instanceof BodyParameter) {
                body = (BodyParameter) parameter;
            }
        }
        
        for (Map.Entry<String, Response> entry : operation.getResponses().entrySet()) {
            responses.add(new ResponseModel(Integer.parseInt(entry.getKey()), entry.getValue()));
            Property schema = entry.getValue().getSchema();
            if(schema instanceof RefProperty) {
                responseSchema = definitions.get(((RefProperty) schema).getSimpleRef());
            }
        }
    }

    public List<String> getTags() {
        if(operation.getTags() == null) {
            return Collections.emptyList();
        }
        return operation.getTags();
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
    
    public String getDescriptionHtml() {
        return operation.getDescription();
    }

    public String getJsonOutput() {
        return jsonOutput;
    }

    public List<ExampleModel> getExamples() {
        return examples;
    }
    
    public List<ResponseModel> getResponses() {
        return responses;
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
        File file = new File("clients/docs/src/main/examples/" + getId() + ext);
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

    public DefinitionModel getResponseSchema() {
        return responseSchema;
    }
}
