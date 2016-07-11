package org.activityinfo.api.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import io.swagger.models.Model;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import org.activityinfo.model.form.annotation.Field;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Map;


public class ModelGenerator {


    private final DataTypeFactory dataTypeFactory;
    private final String modelName;
    private final Model model;

    private final ClassName className;
    private final TypeSpec.Builder classBuilder;
    private final MethodSpec.Builder constructor;
    private final MethodSpec.Builder parseMethod;

    public ModelGenerator(DataTypeFactory dataTypeFactory, String modelName, Model model) {

        this.dataTypeFactory = dataTypeFactory;
        this.className = ClassName.get(ModelDataType.MODEL_PACKAGE, modelName);
        this.modelName = modelName;
        this.model = model;

        classBuilder = TypeSpec.classBuilder(modelName)
                .addModifiers(Modifier.PUBLIC);

        constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);


        parseMethod = MethodSpec.methodBuilder("fromJson")
                .addParameter(ClassName.get(JsonElement.class), "jsonElement")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("$T jsonObject = jsonElement.getAsJsonObject()", JsonObject.class)
                .addStatement("$T model = new $T()", className, className)
                .returns(className);


        for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
            handleProperty(entry.getKey(), entry.getValue());
        }
        

        //generateSetters();
        
    }

    private void handleProperty(String name, Property property) {
        DataType dataType = dataTypeFactory.get(property);
        
        // Add a field for this property
        FieldSpec field = FieldSpec.builder(dataType.getReturnTypeName(), name, Modifier.PRIVATE).build();
        classBuilder.addField(field);
        
        // Add a getter for the property...
        classBuilder.addMethod(MethodSpec.methodBuilder(accessor(name, property))
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return $N", field)
            .returns(dataType.getReturnTypeName())
            .build());
        
        // And ensure that it is parsed
        CodeBlock jsonElementExpr = CodeBlock.of("jsonObject.get($S)", name);
        parseMethod.addStatement("model.$L = $L", field.name, dataType.fromJsonElement(jsonElementExpr));
        
    }

    private String accessor(String name, Property property) {
        String upperCase = name.substring(0, 1).toUpperCase() + name.substring(1);
        if(property instanceof BooleanProperty) {
            return "is" + upperCase;
        } else {
            return "get" + upperCase;
        }
    }

    public void writeTo(File outputDir) throws IOException {
        
        parseMethod.addStatement("return model");
        
        classBuilder.addMethod(constructor.build());
        classBuilder.addMethod(parseMethod.build());

        JavaFile javaFile = JavaFile.builder(ModelDataType.MODEL_PACKAGE, classBuilder.build()).build();
        javaFile.writeTo(outputDir);
    }
}
