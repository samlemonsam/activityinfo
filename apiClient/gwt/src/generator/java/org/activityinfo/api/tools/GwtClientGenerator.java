package org.activityinfo.api.tools;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.squareup.javapoet.*;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates the sources for the GWT API client
 */
public class GwtClientGenerator {

    private static final String CLIENT_PACKAGE = "org.activityinfo.api.client";

    private final Swagger spec;
    private DataTypeFactory dataTypeFactory;
    private File outputDir;

    public static void main(String[] args) throws IOException {

        File specFile;
        File outputDir;
        if(args.length == 2) {
            specFile = new File(args[0]);
            outputDir = new File(args[1]);
        } else {
            specFile = new File("api/build/api.json");
            outputDir = new File("apiClient/gwt/build/generated");
        }

        if(!specFile.exists()) {
            System.err.println("Input file " + specFile.getAbsolutePath() + " does not exist.");
            System.exit(-1);
        }

        if(!outputDir.exists()) {
            outputDir.mkdirs();
        }

        System.out.println("Generating sources in " + outputDir);

        GwtClientGenerator generator = new GwtClientGenerator(specFile, outputDir);
        generator.generate();
    }

    public GwtClientGenerator(File specFile, File outputDir) {
        this.outputDir = outputDir;
        spec = new SwaggerParser().read(specFile.getAbsolutePath());
        dataTypeFactory = new DataTypeFactory(spec);
    }

    private void generate() throws IOException {
        generateClientInterface();
        generateClientImpl();
        generateBuilders();
    }

    private void generateClientInterface() throws IOException {
        TypeSpec.Builder clientInterface = TypeSpec.interfaceBuilder("ActivityInfoClientAsync")
                .addModifiers(Modifier.PUBLIC);


        for (Map.Entry<String, Path> pathEntry : spec.getPaths().entrySet()) {
            Path path = pathEntry.getValue();
            for (Map.Entry<HttpMethod, Operation> entry : path.getOperationMap().entrySet()) {
                if(isIncluded(entry.getValue())) {
                    clientInterface.addMethod(generateOperationMethod(pathEntry.getKey(), entry.getKey(), entry.getValue(), false));
                }
            }
        }

        JavaFile javaFile = JavaFile.builder(CLIENT_PACKAGE, clientInterface.build()).build();

        javaFile.writeTo(outputDir);
    }

    private void generateClientImpl() throws IOException {
        TypeSpec.Builder clientClass = TypeSpec.classBuilder("ActivityInfoClientAsyncImpl")
                .addSuperinterface(ClassName.get(CLIENT_PACKAGE, "ActivityInfoClientAsync"))
                .addModifiers(Modifier.PUBLIC);

        clientClass.addField(FieldSpec.builder(String.class, "BASE_URL")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", spec.getBasePath())
                .build());

        clientClass.addField(FieldSpec.builder(Logger.class, "LOGGER")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.getLogger(ActivityInfoClientAsync.class.getName())", Logger.class)
                .build());

        for (Map.Entry<String, Path> pathEntry : spec.getPaths().entrySet()) {
            Path path = pathEntry.getValue();
            for (Map.Entry<HttpMethod, Operation> entry : path.getOperationMap().entrySet()) {
                if(isIncluded(entry.getValue())) {
                    clientClass.addMethod(generateOperationMethod(pathEntry.getKey(), entry.getKey(), entry.getValue(), true));
                }
            }
        }

        JavaFile javaFile = JavaFile.builder(CLIENT_PACKAGE, clientClass.build())
                .build();

        javaFile.writeTo(outputDir);
    }

    private boolean isIncluded(Operation operation) {
        return !Boolean.FALSE.equals(operation.getVendorExtensions().get("x-gwt-client"));
    }

    private MethodSpec generateOperationMethod(String path, HttpMethod httpMethod, Operation operation,
                                               boolean implementation) {

        DataType responseType = findResponseType(operation);

        MethodSpec.Builder method = MethodSpec.methodBuilder(operation.getOperationId())
                .returns(responseType.getPromisedReturnType());

        if(implementation) {
            method.addModifiers(Modifier.PUBLIC);
        } else {
            method.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        }

        if(operation.getSummary() != null) {
            method.addJavadoc(operation.getSummary());
            method.addJavadoc("\n\n");
        }
        if(operation.getDescription() != null) {
            method.addJavadoc(operation.getDescription());
            method.addJavadoc("\n");
        }

        Parameter bodyParameter = null;

        for (Parameter parameter : operation.getParameters()) {
            DataType type = dataTypeFactory.get(parameter);
            ParameterSpec.Builder param = ParameterSpec.builder(type.getParameterType(), parameter.getName());
            method.addParameter(param.build());

            if(parameter.getIn().equals("body")) {
                bodyParameter = parameter;
            }

            if(parameter.getDescription() != null) {
                method.addJavadoc("@param ");
                method.addJavadoc(parameter.getName());
                method.addJavadoc(" ");
                method.addJavadoc(parameter.getDescription());
                method.addJavadoc("\n");
            }

        }

        if(implementation) {
            // Classes that we use
            ClassName requestBuilder = ClassName.get(RequestBuilder.class);

            method.addStatement("final $T result = new Promise<>()", responseType.getPromisedReturnType());

            method.addStatement("final String url = $L", buildPathExpr(path));
            method.addStatement("$T requestBuilder = new $T($T.$L, url)",
                    requestBuilder, requestBuilder, requestBuilder, httpMethod.name().toUpperCase());

            if (bodyParameter != null) {
                DataType bodyType = dataTypeFactory.get(bodyParameter);
                method.addStatement("requestBuilder.setRequestData($L)", bodyType.toJsonString(bodyParameter.getName()));
            }

            method.addStatement("requestBuilder.setCallback($L)", generateCallback(operation));

            method.addStatement("return result");
        }

        return method.build();
    }

    private TypeSpec generateCallback(Operation operation) {

        TypeSpec.Builder callback = TypeSpec.anonymousClassBuilder("")
                .superclass(RequestCallback.class);

        MethodSpec.Builder onReceived = MethodSpec.methodBuilder("onResponseReceived")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Request.class, "request")
                .addParameter(com.google.gwt.http.client.Response.class, "response")
                .returns(void.class);

        for (Map.Entry<String, Response> entry : operation.getResponses().entrySet()) {
            Response expectedResponse = entry.getValue();
            int statusCode = Integer.parseInt(entry.getKey());

            if(statusCode >= 200 && statusCode < 300) {
                onReceived.beginControlFlow("if(response.getStatusCode() == $L)", statusCode);
                if (expectedResponse.getSchema() == null) {
                    onReceived.addStatement("result.resolve(null)");
                } else {
                    DataType returnType = dataTypeFactory.get(expectedResponse.getSchema());
                    onReceived.addStatement("result.resolve($L)", returnType.fromJsonString("response.getText()"));
                }
                onReceived.addStatement("return");
                onReceived.endControlFlow();
            }
        }
        // If the response did not match a success case, then treat it as an error
        onReceived.addStatement("result.reject(new RuntimeException(\"Status code: \" + response.getStatusCode()))");

        MethodSpec.Builder onFailed = MethodSpec.methodBuilder("onError")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Request.class, "request")
                .addParameter(Throwable.class, "error")
                .addStatement("LOGGER.log($T.SEVERE, \"Request to \" + url + \" failed: \" + error.getMessage(), error)",
                        ClassName.get(Level.class))
                .addStatement("result.reject(error)")
                .returns(void.class);


        callback.addMethod(onReceived.build());
        callback.addMethod(onFailed.build());

        return callback.build();
    }

    private String buildPathExpr(String path) {
        String[] parts = path.split("/");
        StringBuilder expr = new StringBuilder();
        expr.append("BASE_URL");
        for (String part : parts) {
            if(!part.isEmpty()) {
                expr.append(" + ");
                if(part.startsWith("{")) {
                    String paramName = part.substring("{".length(), part.length() - "}".length());
                    expr.append("\"/\" + ");
                    expr.append(paramName);
                } else {
                    expr.append("\"/").append(part).append("\"");
                }
            }
        }
        return expr.toString();
    }

    private DataType findResponseType(Operation operation) {
        for (Response response : operation.getResponses().values()) {
            if(response.getSchema() != null) {
                return dataTypeFactory.get(response.getSchema());
            }
        }
        return new VoidDataType();
    }


    private void generateBuilders() throws IOException {
        Set<String> parameterModels = findParameterModelTypes();
        for (Map.Entry<String, Model> entry : spec.getDefinitions().entrySet()) {
            String modelName = entry.getKey();
            if( !ProvidedModel.isProvided(modelName) &&
                 parameterModels.contains(modelName)) {
                
                BuilderGenerator builderGenerator = new BuilderGenerator(dataTypeFactory, modelName, entry.getValue());
                builderGenerator.writeTo(outputDir);
            }
        }
    }

    private Set<String> findParameterModelTypes() {
        Set<String> models = new HashSet<>();

        for (Path path : spec.getPaths().values()) {
            for (Operation operation : path.getOperations()) {
                for (Parameter parameter : operation.getParameters()) {
                    if (parameter instanceof BodyParameter) {
                        BodyParameter bodyParameter = (BodyParameter) parameter;
                        if (bodyParameter.getSchema() instanceof RefModel) {
                            RefModel refModel = (RefModel) bodyParameter.getSchema();
                            models.add(refModel.getSimpleRef());
                        }
                    }
                }
            }
        }
        return models;
    }
}
