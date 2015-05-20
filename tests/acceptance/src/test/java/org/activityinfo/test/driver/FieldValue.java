package org.activityinfo.test.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValue {
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[-\\d\\.,+]+");
    
    private String field;
    private String value;

    public FieldValue() {
    }

    public FieldValue(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public FieldValue(String field, Double value) {
        this.field = field;
        this.value = Double.toString(value);
    }

    public FieldValue(String fieldName, int value) {
        this.field = fieldName;
        this.value = Integer.toString(value);
    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public void setValue(double value) {
        this.value = Double.toString(value);
    }

    public Object maybeNumberValue() {
        try {
            Matcher matcher = NUMBER_PATTERN.matcher(value);
            if(matcher.find()) {
                return Double.parseDouble(matcher.group());
            } 
        } catch (NumberFormatException ignored) {
        }
        return value;
    }
    
    public Double asDouble() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "The property '%s' with value '%s' is not an number: %s",
                    field, value, e.getMessage()), e);
        }
    }
    
    public static Map<String, FieldValue> toMap(Iterable<FieldValue> values) {
        Map<String, FieldValue> map = new HashMap<>();
        for(FieldValue value : values) {
            map.put(value.getField(), value);
        }
        return map;
    }
}
