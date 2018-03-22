package org.activityinfo.model;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes("org.activityinfo.json.AutoJson")
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
                        writeJsonSerializer(roundEnv, (TypeElement) modelClass);
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

    private void writeJsonSerializer(RoundEnvironment roundEnv, TypeElement modelClass) throws IOException {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing " + modelClass.getSimpleName());

        MetaModel metaModel = new MetaModel(modelClass);

        ClassName jsonClassName = ClassName.get(metaModel.getPackageName(), metaModel.getSimpleName() + "Json");

        MethodSpec toJson = MethodSpec.methodBuilder("toJson")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(TypeName.get(metaModel.getModelTypeMirror()), "model")
                .addStatement("throw new $T()", UnsupportedOperationException.class)
                .build();

        TypeSpec jsonSerializer = TypeSpec.classBuilder(jsonClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(toJson)
                .build();

        JavaFile javaFile = JavaFile.builder(jsonClassName.packageName(), jsonSerializer)
                .build();


        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(jsonClassName.reflectionName(), modelClass);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            javaFile.writeTo(out);
        }
    }
}
