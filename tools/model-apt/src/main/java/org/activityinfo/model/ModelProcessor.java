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
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

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

        List<BuilderTarget> targets = metaModel.getBuilderTargets();

        TypeSpec builderClass = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(modelField)
                .addMethods(targets.stream().flatMap(t -> t.builderMethods(builderClassName)).collect(toList()))
                .addMethod(buildMethod(metaModel, targets))
                .build();


        JavaFile javaFile = JavaFile.builder(builderClassName.packageName(), builderClass)
                .build();

        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(builderClassName.reflectionName(), modelClass);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            javaFile.writeTo(out);
        }
    }

    private MethodSpec buildMethod(MetaModel metaModel, List<BuilderTarget> targets) {

        MethodSpec.Builder method = MethodSpec.methodBuilder("build")
                .returns(metaModel.getTypeName())
                .addAnnotation(Nonnull.class)
                .addModifiers(Modifier.PUBLIC);

        for (BuilderTarget target : targets) {
            for (CodeBlock codeBlock : target.assertions()) {
                method.addStatement(codeBlock);
            }
        }
        method.addStatement("return this.model");
        return method.build();
    }


    private void writeJsonClass(TypeElement modelClass) throws IOException {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing JSON for " + modelClass.getSimpleName());

        MetaModel metaModel = new MetaModel(modelClass);

        ClassName jsonClassName = ClassName.get(metaModel.getPackageName(), metaModel.getSimpleName() + "Json");


        TypeSpec jsonClass = TypeSpec.classBuilder(jsonClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(buildToJsonMethod(metaModel))
                .addMethod(buildFromJsonMethod(metaModel))
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


    private MethodSpec buildFromJsonMethod(MetaModel metaModel) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("fromJson")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(metaModel.getTypeName())
                .addParameter(TypeName.get(JsonValue.class), "object");

        method.addStatement("$T model = new $T()", metaModel.getTypeName(), metaModel.getTypeName());

        for (BuilderTarget builderTarget : metaModel.getBuilderTargets()) {
            builderTarget.addFromJson(method);
        }

        method.addStatement("return model");

        return method.build();
    }
}
