package org.activityinfo.model.type;

import com.google.common.base.Strings;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.primitive.HasStringValue;

public class NarrativeValue implements FieldValue, HasStringValue {

    private String text;

    private NarrativeValue(String text) {
        this.text = text;
    }

    public static NarrativeValue valueOf(String text) {
        if(Strings.isNullOrEmpty(text)) {
            return null;
        } else {
            return new NarrativeValue(text);
        }
    }

    public String getText() {
        return text;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return NarrativeType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJsonElement() {
        return Json.create(text);
    }


    @Override
    public String asString() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NarrativeValue that = (NarrativeValue) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

}
