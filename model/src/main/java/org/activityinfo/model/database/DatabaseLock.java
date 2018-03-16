package org.activityinfo.model.database;

import org.activityinfo.model.date.LocalDateRange;

public class DatabaseLock {
    private String id;
    private String label;
    private String resourceId;
    private LocalDateRange lockRange;

    public DatabaseLock(String id, String label, String resourceId, LocalDateRange lockRange) {
        this.id = id;
        this.label = label;
        this.resourceId = resourceId;
        this.lockRange = lockRange;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LocalDateRange getLockRange() {
        return lockRange;
    }
}
