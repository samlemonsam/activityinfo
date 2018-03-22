package org.activityinfo.model.meta;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;

public enum ModelSerializationStrategy {
    JSON_SERIALIZABLE {
        @Override
        public CodeBlock toJsonValue(TypeName typeName, CodeBlock value) {
            return CodeBlock.of("$L.toJson()", value);
        }

        @Override
        public CodeBlock fromJson(TypeName typeName, CodeBlock jsonValue) {
            return CodeBlock.of("$T.fromJson($L)", typeName, jsonValue);
        }
    },
    NONE {
        @Override
        public CodeBlock toJsonValue(TypeName typeName, CodeBlock value) {
            throw new UnsupportedOperationException(typeName + " cannot be serialized to JSON.");
        }

        @Override
        public CodeBlock fromJson(TypeName typeName, CodeBlock jsonValue) {
            throw new UnsupportedOperationException(typeName + " cannot be serialized to JSON.");
        }
    };

    public static ModelSerializationStrategy of(DeclaredType declaredType) {
        if(MetaTypes.inherits(declaredType, "org.activityinfo.json.JsonSerializable")) {
            return JSON_SERIALIZABLE;
        }
        return NONE;
    }

    public abstract CodeBlock toJsonValue(TypeName typeName, CodeBlock value);

    public abstract CodeBlock fromJson(TypeName typeName, CodeBlock jsonValue);
}
