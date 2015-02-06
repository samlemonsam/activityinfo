package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import org.activityinfo.test.driver.Property;

import java.util.HashMap;
import java.util.Map;

public class TestProps {
    private Map<String, Object> map = new HashMap<>();
    
    public TestProps(Property... properties) {
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

        return (String)defaultValue;
    }
}
