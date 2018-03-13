package org.activityinfo.model.database;

public class DatabaseForm {
    private String id;
    private String parentId;
    private String label;

    public DatabaseForm(String id, String parentId, String label) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getLabel() {
        return label;
    }
}
