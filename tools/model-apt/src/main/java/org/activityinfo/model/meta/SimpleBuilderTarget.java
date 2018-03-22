package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SimpleBuilderTarget extends BuilderTarget {
    private final VariableElement field;
    private final MetaType type;

    public SimpleBuilderTarget(VariableElement field, MetaType type) {
        this.field = field;
        this.type = type;
    }

    @Override
    public Stream<MethodSpec> builderMethods(TypeName builderClassName) {

        ParameterSpec.Builder valueParameter = ParameterSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString());
        if(!type.getTypeName().isPrimitive()) {
            valueParameter.addAnnotation(Nonnull.class);
        }

        return Stream.of(MethodSpec.methodBuilder(Names.methodName("set", field))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(valueParameter.build())
                .addAnnotation(Nonnull.class)
                .returns(builderClassName)
                .addStatement("this.model.$N = $N", field.getSimpleName(), field.getSimpleName())
                .addStatement("return this")
                .build());
    }

    @Override
    public void addFromJson(MethodSpec.Builder method) {
        method.addStatement(CodeBlock.of("model.$N = $L", field.getSimpleName(),
                type.fromJsonProperty(CodeBlock.of("object"), field.getSimpleName().toString())));
    }

    @Override
    public List<CodeBlock> assertions() {
        if(type.getTypeName().isPrimitive()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(CodeBlock.of("assert model.$N != null : $S", field.getSimpleName(),
                field.getSimpleName() + " is missing"));
    }
}
