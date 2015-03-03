package org.activityinfo.test.driver;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.junit.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestObject {
    private Map<String, Object> map = new HashMap<>();

    public TestObject(Property... properties) {
        for(Property p : properties) {
            map.put(p.getKey(), p.getValue());
        }
    }

    public String getString(String propertyName) {
        return get(propertyName, String.class);
    }

    public String getString(String propertyName, String defaultValue) {
        return get(propertyName, String.class, defaultValue);
    }

    public LocalDate getDate(String propertyName) {
        return get(propertyName, LocalDate.class);
    }

    public LocalDate getDate(String propertyName, LocalDate defaultValue) {
        return get(propertyName, LocalDate.class, defaultValue);
    }

    public boolean has(String propertyName) {
        return map.containsKey(propertyName);
    }

    public List<String> getStringList(String propertyName) {
        Object value = map.get(propertyName);
        if(value instanceof String) {
            return Collections.singletonList((String)value);
            
        } else if(value instanceof Iterable) {
            Iterable<?> items = get(propertyName, Iterable.class);
            List<String> list = Lists.newArrayList();

            for (Object item : items) {
                if(item instanceof String) {
                    list.add((String)item);
                } else {
                    throw new AssertionError(String.format("Expected list of Strings, but property '%s' contained '%s' " +
                            "of type '%s'", propertyName, item.toString(), item.getClass().getName()));
                }
            }
            return list;   
        } else {
            throw new AssertionError(String.format("Property '%s' must be provided", propertyName));
        }
    }

    private <T> T get(String propertyName, Class<T> valueClass) {
        return get(propertyName, valueClass, null);
    }

    private <T> T get(String propertyName, Class<T> valueClass, T defaultValue) {
        Object value = map.get(propertyName);

        if(value == null) {
            if(defaultValue != null) {
                return defaultValue;
            } else {
                throw new AssertionError(String.format("Property '%s' must be provided", propertyName));
            }
        } else {

            Preconditions.checkState(valueClass.isAssignableFrom(value.getClass()),
                    "Property '%s' must an instance of %s", propertyName,
                    valueClass.getName());

            return valueClass.cast(value);
        }
    }

}
