package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class BuilderTarget {

    public abstract Stream<MethodSpec> builderMethods(TypeName builderClassName);

    public List<CodeBlock> assertions() {
        return Collections.emptyList();
    }
}
