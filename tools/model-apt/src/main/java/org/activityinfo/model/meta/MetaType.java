package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public interface MetaType {


    TypeName getTypeName();

    /**
     *
     * @param value a code block that yields a value of this type
     * @return a code block that transforms a value of this type to a value that can be supplied
     * to {@code JsonValue.put()}
     */
    CodeBlock toJsonPuttable(CodeBlock value);

    /**
     *
     * @param value a code block that yields a value that is an Iterable of this type
     * @return a code block that transforms an Iterable of this type to a JsonValue
     */
    CodeBlock toJsonArray(CodeBlock value);


    /**
     *
     * @param jsonValue a code block that yields a JsonValue of type "object"
     * @param name the property name to accces
     * @return a code block that yields an instance of this type, converted from the Json property
     */
    CodeBlock fromJsonProperty(CodeBlock jsonValue, String name);

    CodeBlock fromJsonValue(CodeBlock jsonValue);

    CodeBlock getJsonSerializerMethodRef();


}
