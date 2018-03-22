package org.activityinfo.model.meta;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.util.Optional;

public class MetaProperty {

    private final TypeElement modelClass;
    private final VariableElement fieldElement;
    private final Optional<ExecutableElement> getter;

    public MetaProperty(TypeElement modelClass, VariableElement fieldElement) {
        this.modelClass = modelClass;
        this.fieldElement = fieldElement;
        this.getter = findGetter(modelClass, fieldElement);
    }

    private Optional<ExecutableElement> findGetter(TypeElement modelClass, VariableElement fieldElement) {
        return ElementFilter.methodsIn(modelClass.getEnclosedElements())
                .stream()
                .filter(m ->  m.getSimpleName().toString().equalsIgnoreCase("get" + fieldElement.getSimpleName()) &&
                              m.getModifiers().contains(Modifier.PUBLIC) &&
                             !m.getModifiers().contains(Modifier.STATIC) &&
                              m.getParameters().isEmpty())
                .findAny();
    }


    public String getName() {
        return fieldElement.getSimpleName().toString();
    }

    public Optional<ExecutableElement> getGetter() {
        return getter;
    }
}
