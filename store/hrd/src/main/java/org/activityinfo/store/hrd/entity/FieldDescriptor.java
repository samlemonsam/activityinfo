package org.activityinfo.store.hrd.entity;

public class FieldDescriptor {

    private String columnId;
    private long version;

    public FieldDescriptor() {
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean hasBlockAssignment() {
        return columnId != null;
    }

    @Override
    public String toString() {
        return "FieldDescriptor{" +
                "columnId='" + columnId + '\'' +
                ", version=" + version +
                '}';
    }
}
