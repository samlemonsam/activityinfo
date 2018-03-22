package org.activityinfo.model;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class MetaModel {

    private final TypeElement modelClass;
    private String packageName;
    private final String className;

    public MetaModel(TypeElement modelClass) {

        this.modelClass = modelClass;
        className = modelClass.getQualifiedName().toString();

        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }
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
}
