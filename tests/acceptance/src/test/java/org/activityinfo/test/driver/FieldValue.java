package org.activityinfo.test.driver;

public class FieldValue {
    private String field;
    private String value;

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

    public Object maybeNumberValue() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }
}
