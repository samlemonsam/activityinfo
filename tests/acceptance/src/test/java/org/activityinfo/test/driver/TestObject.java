package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import org.activityinfo.test.driver.Property;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestObject {
    private Map<String, Object> map = new HashMap<>();
    
    public TestObject(Property... properties) {
        for(Property p : properties) {
            map.put(p.getKey(), p.getValue());
        }
    }
    
    public String getString(String propertyName) {
        Object value = map.get(propertyName);
        
        Preconditions.checkState(value != null, "Property '%s' must be provided", propertyName);
        Preconditions.checkState(value instanceof String, "Property '%s' must be a string", propertyName);
        
        return (String) value;
    }

    public String getString(String propertyName, String defaultValue) {
        Object value = map.get(propertyName);
        if(value == null) {
            return defaultValue;
        }
        Preconditions.checkState(value instanceof String, "Property '%s' must be a string", propertyName);

        return (String) value;
    }

    public LocalDate getDate(String propertyName) {
        Object value = map.get(propertyName);

        Preconditions.checkState(value != null, "Property '%s' must be provided", propertyName);
        Preconditions.checkState(value instanceof LocalDate, "Property '%s' must an instance of %s", propertyName, 
                LocalDate.class.getName());

        return (LocalDate) value;
    }

    public LocalDate getDate(String propertyName, LocalDate defaultValue) {
        Object value = map.get(propertyName);
        if(value == null) {
            return defaultValue;
        }

        Preconditions.checkState(value instanceof LocalDate, "Property '%s' must an instance of %s", propertyName,
                LocalDate.class.getName());

        return (LocalDate) value;        
    }

    public boolean has(String propertyName) {
        return map.containsKey(propertyName);
    }
}
