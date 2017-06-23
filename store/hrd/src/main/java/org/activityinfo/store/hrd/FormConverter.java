package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.collect.Lists;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonArray;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

import java.util.List;
import java.util.Map;

/**
 * Serializes records between AppEngine Entities and back
 */
public class FormConverter {
    
    public static EmbeddedEntity toEmbeddedEntity(JsonObject record) {
        EmbeddedEntity entity = new EmbeddedEntity();
        for (Map.Entry<String, JsonValue> entry : record.entrySet()) {
            entity.setUnindexedProperty(entry.getKey(), toPropertyValue(entry.getValue()));
        }
        return entity;
    }
    
    public static Object toPropertyValue(JsonValue value) {
        if(value.isJsonNull()) {
            return null;
        
        } else if(value.isJsonPrimitive()) {
            if(value.isString()) {
                return value.asString();
            } else if(value.isNumber()) {
                return value.asNumber();
            } else if(value.isBoolean()) {
                return value.asBoolean();
            } else {
                throw new UnsupportedOperationException("type: " + value.getType());
            }
            
        } else if(value.isJsonObject()) {
            return toEmbeddedEntity(value.getAsJsonObject());
            
        } else if(value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            List<Object> propertyList = Lists.newArrayListWithCapacity(array.length());

            for (JsonValue jsonElement : array.values()) {
                propertyList.add(toPropertyValue(jsonElement));
            }
            return propertyList;
            
        } else {
            throw new IllegalArgumentException("value: " + value + " [" + value.getClass().getName() + "]");
        }
    }
    
    public static JsonObject fromEmbeddedEntity(EmbeddedEntity entity) {
        JsonObject record = Json.createObject();
        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            if(entry.getValue() != null) {
                record.add(entry.getKey(), fromPropertyValue(entry.getValue()));
            }
        }
        return record;
    }

    public static JsonValue fromPropertyValue(Object propertyValue) {
        if(propertyValue == null) {
            return Json.createNull();
            
        } else if(propertyValue instanceof EmbeddedEntity) {
            return fromEmbeddedEntity(((EmbeddedEntity) propertyValue));
            
        } else if(propertyValue instanceof List) {
            List<Object> propertyValueList = (List<Object>) propertyValue;
            JsonArray convertedList = Json.createArray();
            for (Object propertyValueListItem : propertyValueList) {
                convertedList.add(fromPropertyValue(propertyValueListItem));
            }
            return convertedList;
            
        } else if(propertyValue instanceof String) {
            return Json.create((String)propertyValue);
        
        } else if(propertyValue instanceof Number) {
            return Json.create(((Number) propertyValue).doubleValue());
        
        } else if(propertyValue instanceof Boolean) {
            return Json.create((Boolean) propertyValue);
        
        } else {
            throw new UnsupportedOperationException("type: " + propertyValue.getClass().getName());
        }
    }
}
