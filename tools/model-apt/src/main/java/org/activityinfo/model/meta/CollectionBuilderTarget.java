package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.activityinfo.json.JsonValue;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.stream.Stream;

public class CollectionBuilderTarget extends BuilderTarget {
    private final VariableElement field;
    private final CollectionMetaType setType;

    public CollectionBuilderTarget(VariableElement field, CollectionMetaType setType) {
        this.field = field;
        this.setType = setType;
    }

    @Override
    public Stream<MethodSpec> builderMethods(TypeName builderClassName) {
        ParameterSpec elementParameter = ParameterSpec.builder(setType.getElementType().getTypeName(), "element")
                .addAnnotation(Nonnull.class)
                .build();

        return Stream.of(MethodSpec.methodBuilder(Names.methodName("add", Names.singularize(field.getSimpleName())))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(elementParameter)
                .addAnnotation(Nonnull.class)
                .returns(builderClassName)
                .addStatement("this.model.$N.add($N)", field.getSimpleName(), "element")
                .addStatement("return this")
                .build());
    }

    @Override
    public void addFromJson(MethodSpec.Builder method) {
        method.beginControlFlow("");
        method.addStatement("$T array = object.get($S)", JsonValue.class, field.getSimpleName());
        method.beginControlFlow("for(int i=0; i<array.length();++i)");
        method.addStatement("model.$N.add($L)", field.getSimpleName(),
                setType.getElementType().fromJsonValue(CodeBlock.of("array.get(i)")));
        method.endControlFlow();
        method.endControlFlow();
    }
}
