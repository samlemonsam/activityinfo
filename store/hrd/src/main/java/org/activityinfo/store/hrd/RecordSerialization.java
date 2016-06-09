package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.common.collect.Lists;
import org.activityinfo.model.resource.PropertyBag;
import org.activityinfo.model.resource.Record;

import java.util.List;
import java.util.Map;

/**
 * Serializes records between AppEngine Entities and back
 */
public class RecordSerialization {
    
    public static EmbeddedEntity toEmbeddedEntity(PropertyBag record) {
        EmbeddedEntity entity = new EmbeddedEntity();
        Map<String, Object> properties = record.getProperties();
        for (Map.Entry<String, Object> entry :  properties.entrySet()) {
            entity.setUnindexedProperty(entry.getKey(), toPropertyValue(entry.getValue()));
        }
        return entity;
    }
    
    private static Object toPropertyValue(Object value) {
        if(value instanceof String) {
            return value;
        } else if(value instanceof Number) {
            return value;
        } else if(value instanceof Boolean) {
            return value;
        } else if(value instanceof Record) {
            return toEmbeddedEntity(((Record) value));
        } else if(value instanceof List) {
            List<Object> valueList = (List<Object>) value;
            List<Object> propertyList = Lists.newArrayListWithCapacity(valueList.size());

            for (Object valueListItem : valueList) {
                propertyList.add(toPropertyValue(valueListItem));
            }
            return propertyList;
            
        } else {
            throw new IllegalArgumentException("value: " + value + " [" + value.getClass().getName() + "]");
        }
    }
    
    public static Record fromEmbeddedEntity(EmbeddedEntity entity) {
        Record record = new Record();
        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            record.set(entry.getKey(), fromPropertyValue(entry.getValue()));
        }
        return record;
    }

    private static Object fromPropertyValue(Object propertyValue) {
        if(propertyValue instanceof EmbeddedEntity) {
            return fromEmbeddedEntity(((EmbeddedEntity) propertyValue));
        } else if(propertyValue instanceof List) {
            List<Object> propertyValueList = (List<Object>) propertyValue;
            List<Object> convertedList = Lists.newArrayListWithCapacity(propertyValueList.size());
            for (Object propertyValueListItem : propertyValueList) {
                convertedList.add(fromPropertyValue(propertyValueListItem));
            }
            return convertedList;
        } else {
            return propertyValue;
        }
    }
}
