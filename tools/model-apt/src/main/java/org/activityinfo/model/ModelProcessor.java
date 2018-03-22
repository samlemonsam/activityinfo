package org.activityinfo.model;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.meta.*;

import javax.annotation.Nonnull;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "org.activityinfo.json.AutoJson",
        "org.activityinfo.model.annotation.AutoBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "Starting json annotation processor: " + annotations);

        boolean claimed = false;
        for (TypeElement annotation : annotations) {

            if (annotation.getQualifiedName().contentEquals("org.activityinfo.json.AutoJson")) {
                Set<? extends Element> modelClasses = roundEnv.getElementsAnnotatedWith(annotation);

                for (Element modelClass : modelClasses) {
                    try {
                        writeJsonClass((TypeElement) modelClass);
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Failed to generate class for " + modelClass, modelClass);
                    }
                }
                claimed = true;
            }
            if (annotation.getQualifiedName().contentEquals("org.activityinfo.model.annotation.AutoBuilder")) {
                Set<? extends Element> modelClasses = roundEnv.getElementsAnnotatedWith(annotation);

                for (Element modelClass : modelClasses) {
                    try {
                        writeBuilder(roundEnv, (TypeElement) modelClass);
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Failed to generate class for " + modelClass, modelClass);
                    }
                }
                claimed = true;
            }
        }
        return claimed;
    }

    private void writeBuilder(RoundEnvironment roundEnv, TypeElement modelClass) throws IOException {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing Builder for " + modelClass.getSimpleName());

        MetaModel metaModel = new MetaModel(modelClass);

        ClassName builderClassName = ClassName.get(metaModel.getPackageName(),
                metaModel.getSimpleName() + "Builder");

        FieldSpec modelField = FieldSpec.builder(metaModel.getTypeName(), "model", Modifier.FINAL, Modifier.PRIVATE)
                .initializer(CodeBlock.of("new $T()", metaModel.getTypeName()))
                .build();

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .returns(metaModel.getTypeName())
                .addAnnotation(Nonnull.class)
                .addStatement(CodeBlock.of("return this.model"))
                .build();

        TypeSpec builderClass = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(modelField)
                .addMethods(builderMethods(builderClassName, metaModel))
                .addMethod(buildMethod)
                .build();


        JavaFile javaFile = JavaFile.builder(builderClassName.packageName(), builderClass)
                .build();

        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(builderClassName.reflectionName(), modelClass);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            javaFile.writeTo(out);
        }
    }

    private Iterable<MethodSpec> builderMethods(ClassName builderClassName, MetaModel metaModel) {
        List<MethodSpec> methods = new ArrayList<>();

        for (VariableElement field : metaModel.getPackageProtectedFields()) {

            MetaType metaType = MetaTypes.of(field.asType());

            if(metaType instanceof SetMetaType) {
                methods.add(builderAddMethod(builderClassName, field, ((SetMetaType) metaType)));

            } else if(!field.getModifiers().contains(Modifier.FINAL)) {
                methods.add(builderSetMethod(builderClassName, field));
            }
        }

        return methods;
    }

    private MethodSpec builderSetMethod(ClassName builderClassName, VariableElement field) {

        ParameterSpec valueParameter = ParameterSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString())
                .addAnnotation(Nonnull.class)
                .build();

        return MethodSpec.methodBuilder(methodName("set", field))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(valueParameter)
                .addAnnotation(Nonnull.class)
                .returns(builderClassName)
                .addStatement("this.model.$N = $N", field.getSimpleName(), field.getSimpleName())
                .addStatement("return this")
                .build();
    }

    private MethodSpec builderAddMethod(ClassName builderClassName, VariableElement field, SetMetaType setType) {
        ParameterSpec elementParameter = ParameterSpec.builder(setType.getElementType().getTypeName(), "element")
                .addAnnotation(Nonnull.class)
                .build();


        return MethodSpec.methodBuilder(methodName("add", field))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(elementParameter)
                .addAnnotation(Nonnull.class)
                .returns(builderClassName)
                .addStatement("this.model.$N.add($N)", field.getSimpleName(), "element")
                .addStatement("return this")
                .build();
    }

    private String methodName(String prefix, VariableElement field) {
        return methodName(prefix, field.getSimpleName().toString());
    }

    private String methodName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void writeJsonClass(TypeElement modelClass) throws IOException {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing JSON for " + modelClass.getSimpleName());

        MetaModel metaModel = new MetaModel(modelClass);

        ClassName jsonClassName = ClassName.get(metaModel.getPackageName(), metaModel.getSimpleName() + "Json");


        TypeSpec jsonClass = TypeSpec.classBuilder(jsonClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(buildToJsonMethod(metaModel))
                .build();

        JavaFile javaFile = JavaFile.builder(jsonClassName.packageName(), jsonClass)
                .build();


        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(jsonClassName.reflectionName(), modelClass);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            javaFile.writeTo(out);
        }
    }

    private MethodSpec buildToJsonMethod(MetaModel metaModel) {
        MethodSpec.Builder toJson = MethodSpec.methodBuilder("toJson")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(JsonValue.class)
                .addParameter(TypeName.get(metaModel.getModelTypeMirror()), "model");

        toJson.addStatement("$T object = $T.createObject()", JsonValue.class, Json.class);
        for (MetaProperty metaProperty : metaModel.getProperties()) {
            metaProperty.getGetter().ifPresent(getter -> {
                MetaType returnType = MetaTypes.of(getter.getReturnType());
                toJson.addStatement("object.put($S, $L)",
                        metaProperty.getName(),
                        returnType.toJsonPuttable(CodeBlock.of("model.$N()", getter.getSimpleName())));
            });
        }
        toJson.addStatement("return object");
        return toJson.build();
    }
}
