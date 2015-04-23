package org.activityinfo.model.type;

import com.google.common.base.Strings;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.primitive.HasStringValue;

public class NarrativeValue implements FieldValue, IsRecord, HasStringValue {

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
    public Record asRecord() {
        return new Record()
                .set(TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("text", text);
    }

    @Override
    public String asString() {
        return text;
    }

    public static FieldValue fromRecord(Record record) {
        return valueOf(record.getString("text"));
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
