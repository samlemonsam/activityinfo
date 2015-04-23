package org.activityinfo.model.type.enumerated;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;

public class EnumItem implements IsRecord {
    private ResourceId id;
    private String label;
    private String code;

    public EnumItem(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    public ResourceId getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static EnumItem fromRecord(Record record) {
        return new EnumItem(ResourceId.valueOf(record.getString("id")), record.getString("label"))
                .setCode(record.isString("code"));
    }

    public String getCode() {
        return code;
    }

    public EnumItem setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String toString() {
        return id + ":" + label;
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set("label", label)
                .set("code", code)
                .set("id", id.asString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumItem enumItem = (EnumItem) o;

        if (id != null ? !id.equals(enumItem.id) : enumItem.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static ResourceId generateId() {
        KeyGenerator generator = new KeyGenerator();
        return CuidAdapter.attributeField(generator.generateInt());
    }
}
