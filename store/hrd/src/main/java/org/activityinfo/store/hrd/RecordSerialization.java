package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.*;

import java.util.List;
import java.util.Map;

/**
 * Serializes records between AppEngine Entities and back
 */
class RecordSerialization {
    
    public static final String RECORD_PROPERTY = "R";
    public static final String OWNER_PROPERTY = "owner";

    static EmbeddedEntity toEmbeddedEntity(PropertyBag record) {
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
    
    public static Entity toFormClassEntity(FormClass formClass) {
        Entity entity = new Entity(CollectionKeys.formClassKey(formClass.getId()));
        entity.setProperty(OWNER_PROPERTY, formClass.getOwnerId().asString());
        entity.setProperty(RECORD_PROPERTY, toEmbeddedEntity(formClass.asResource()));
        return entity;
    }
    
    public static FormClass fromFormClassEntity(Entity entity) {
        Key collectionKey = entity.getKey().getParent();
        ResourceId collectionId = ResourceId.valueOf(collectionKey.getName());

        Object recordProperty = entity.getProperty(RECORD_PROPERTY);
        if(recordProperty == null) {
            throw new IllegalStateException(String.format("Entity %s is missing record property '%s'",
                    entity.getKey(), RECORD_PROPERTY));
        }
        if(!(recordProperty instanceof EmbeddedEntity)) {
            throw new IllegalArgumentException(String.format("Entity %s has record property '%s' of unexpected type '%s'.",
                    entity.getKey(), RECORD_PROPERTY, recordProperty.getClass().getName()));
        }

        Object ownerValue = entity.getProperty(OWNER_PROPERTY);
        if(!(ownerValue instanceof String)) {
            throw new IllegalStateException(String.format("Entity %s has invalid %s property: %s", 
                    OWNER_PROPERTY, ownerValue));
        }

        Record formClassRecord = fromEmbeddedEntity(((EmbeddedEntity) recordProperty));

        Resource resource = Resources.createResource();
        resource.setId(collectionId);
        resource.setOwnerId(ResourceId.valueOf((String) ownerValue));
        resource.getProperties().putAll(formClassRecord.getProperties());
        return FormClass.fromResource(resource);
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
