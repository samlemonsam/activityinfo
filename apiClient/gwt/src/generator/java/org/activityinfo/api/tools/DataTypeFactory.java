package org.activityinfo.api.tools;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;

public class DataTypeFactory {
    
    private Swagger swagger;

    public DataTypeFactory(Swagger swagger) {
        this.swagger = swagger;
    }

    public DataType get(Property property) {
        if(property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            Model definition = swagger.getDefinitions().get(refProperty.getSimpleRef());
            
            if(ProvidedModel.isProvided(refProperty.getSimpleRef())) {
                return new ProvidedModel(refProperty.getSimpleRef());
            } else {
                return new ModelDataType(refProperty.getSimpleRef(), definition);
            }
        } else if(property instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) property;
            return new ArrayType(get(arrayProperty.getItems()));
        } else if(property instanceof StringProperty) {
            return new PrimitiveDataType(ClassName.get(String.class));
        } else if(property instanceof BooleanProperty) {
            return new PrimitiveDataType(TypeName.BOOLEAN);
        } else if(property instanceof BaseIntegerProperty) {
            return new PrimitiveDataType(TypeName.INT);
        } else if(property instanceof ObjectProperty) {
            return new ObjectType();
        }
        
        throw new UnsupportedOperationException(property.toString());
    }

    public DataType get(Parameter parameter) {
        if(parameter instanceof BodyParameter) {
            Model model = ((BodyParameter) parameter).getSchema();
            if(model instanceof RefModel) {
                String ref = ((RefModel) model).getSimpleRef();
                if(ProvidedModel.isProvided(ref)) {
                    return new ProvidedModel(ref);
                } else {
                    return new ModelDataType(ref, model);
                }
            } else {
                throw new UnsupportedOperationException(parameter.toString());
            }
        } else {
            return new PrimitiveDataType(ClassName.get(String.class));
        }
    }
}
