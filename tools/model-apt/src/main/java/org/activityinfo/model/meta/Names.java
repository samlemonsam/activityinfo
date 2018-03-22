package org.activityinfo.model.meta;

import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;

class Names {

    public static String methodName(String prefix, VariableElement field) {
        return methodName(prefix, field.getSimpleName().toString());
    }

    public static String methodName(String prefix, String name) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String singularize(String name) {
        if(name.endsWith("s")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    public static String singularize(Name name) {
        return singularize(name.toString());
    }
}
