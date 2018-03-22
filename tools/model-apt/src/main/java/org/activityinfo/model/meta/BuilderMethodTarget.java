package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.stream.Stream;

public class BuilderMethodTarget extends BuilderTarget {
    private final ExecutableElement method;

    public BuilderMethodTarget(ExecutableElement method) {
        this.method = method;
    }

    @Override
    public Stream<MethodSpec> builderMethods(TypeName builderClassName) {

        MethodSpec.Builder wrapperMethod = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addAnnotation(Nonnull.class);

        for (VariableElement parameter : method.getParameters()) {
            wrapperMethod.addParameter(ParameterSpec.get(parameter));
        }

        StringBuilder code = new StringBuilder("this.model.");
        code.append(method.getSimpleName());
        code.append("(");

        boolean needsComma = false;
        for (VariableElement parameter : method.getParameters()) {
            if(needsComma) {
                code.append(", ");
            }
            code.append(parameter);
            needsComma = true;
        }
        code.append(")");

        wrapperMethod.addStatement(CodeBlock.of(code.toString()));
        wrapperMethod.addStatement("return this");

        return Stream.of(wrapperMethod.build());
    }
}
