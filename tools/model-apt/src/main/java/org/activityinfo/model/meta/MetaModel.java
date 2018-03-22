package org.activityinfo.model.meta;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class MetaModel {

    private final TypeElement modelClass;
    private final List<MetaProperty> properties;
    private String packageName;
    private final String className;

    public MetaModel(TypeElement modelClass) {

        this.modelClass = modelClass;
        className = modelClass.getQualifiedName().toString();

        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }
        this.properties = fieldsIn(modelClass.getEnclosedElements())
                .stream()
                .map(e -> new MetaProperty(modelClass, e))
                .collect(Collectors.toList());

    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleName() {
        return modelClass.getSimpleName().toString();
    }

    public TypeMirror getModelTypeMirror() {
        return modelClass.asType();
    }

    public List<MetaProperty> getProperties() {
        return properties;
    }

    public TypeName getTypeName() {
        return TypeName.get(modelClass.asType());
    }

    public List<BuilderTarget> getBuilderTargets() {
        List<BuilderTarget> targets = new ArrayList<>();
        for (VariableElement field : fieldsIn(modelClass.getEnclosedElements())) {
            if(!field.getModifiers().contains(Modifier.PRIVATE) &&
               !field.getModifiers().contains(Modifier.STATIC)) {

                MetaType metaType = MetaTypes.of(field.asType());
                if(metaType instanceof CollectionMetaType) {
                    targets.add(new CollectionBuilderTarget(field, (CollectionMetaType)metaType));
                } else {
                    targets.add(new SimpleBuilderTarget(field, metaType));
                }
            }
        }

        for (ExecutableElement method : methodsIn(modelClass.getEnclosedElements())) {
            if(MetaTypes.annotatedWith(method, "org.activityinfo.model.annotation.BuilderMethod")) {
                targets.add(new BuilderMethodTarget(method));
            }
        }

        return targets;
    }

}
