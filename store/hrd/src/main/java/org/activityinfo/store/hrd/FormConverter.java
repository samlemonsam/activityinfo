package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.collect.Lists;
import com.google.gson.*;

import java.util.List;
import java.util.Map;

/**
 * Serializes records between AppEngine Entities and back
 */
public class FormConverter {
    
    public static EmbeddedEntity toEmbeddedEntity(JsonObject record) {
        EmbeddedEntity entity = new EmbeddedEntity();
        for (Map.Entry<String, JsonElement> entry : record.entrySet()) {
            entity.setUnindexedProperty(entry.getKey(), toPropertyValue(entry.getValue()));
        }
        return entity;
    }
    
    public static Object toPropertyValue(JsonElement value) {
        if(value.isJsonNull()) {
            return null;
        
        } else if(value.isJsonPrimitive()) {
            JsonPrimitive primitiveValue = value.getAsJsonPrimitive();
            if(primitiveValue.isString()) {
                return primitiveValue.getAsString();
            } else if(primitiveValue.isNumber()) {
                return primitiveValue.getAsNumber();
            } else if(primitiveValue.isBoolean()) {
                return primitiveValue.getAsBoolean();
            } else {
                throw new UnsupportedOperationException("type: " + primitiveValue.getClass().getName());
            }
            
        } else if(value.isJsonObject()) {
            return toEmbeddedEntity(value.getAsJsonObject());
            
        } else if(value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            List<Object> propertyList = Lists.newArrayListWithCapacity(array.size());

            for (JsonElement jsonElement : array) {
                propertyList.add(toPropertyValue(jsonElement));
            }
            return propertyList;
            
        } else {
            throw new IllegalArgumentException("value: " + value + " [" + value.getClass().getName() + "]");
        }
    }
    
    public static JsonObject fromEmbeddedEntity(EmbeddedEntity entity) {
        JsonObject record = new JsonObject();
        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            if(entry.getValue() != null) {
                record.add(entry.getKey(), fromPropertyValue(entry.getValue()));
            }
        }
        return record;
    }

    public static JsonElement fromPropertyValue(Object propertyValue) {
        if(propertyValue == null) {
            return JsonNull.INSTANCE;
            
        } else if(propertyValue instanceof EmbeddedEntity) {
            return fromEmbeddedEntity(((EmbeddedEntity) propertyValue));
            
        } else if(propertyValue instanceof List) {
            List<Object> propertyValueList = (List<Object>) propertyValue;
            JsonArray convertedList = new JsonArray();
            for (Object propertyValueListItem : propertyValueList) {
                convertedList.add(fromPropertyValue(propertyValueListItem));
            }
            return convertedList;
            
        } else if(propertyValue instanceof String) {
            return new JsonPrimitive((String) propertyValue);
        
        } else if(propertyValue instanceof Number) {
            return new JsonPrimitive((Number) propertyValue);
        
        } else if(propertyValue instanceof Boolean) {
            return new JsonPrimitive((Boolean) propertyValue);
        
        } else {
            throw new UnsupportedOperationException("type: " + propertyValue.getClass().getName());
        }
    }
}
