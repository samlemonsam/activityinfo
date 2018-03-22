package org.activityinfo.model.meta;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.stream.Stream;

public class CollectionBuilderTarget extends BuilderTarget {
    private final VariableElement field;
    private final SetMetaType setType;

    public CollectionBuilderTarget(VariableElement field, SetMetaType setType) {
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


}
