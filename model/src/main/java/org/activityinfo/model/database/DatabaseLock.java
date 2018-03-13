package org.activityinfo.model.database;

import org.activityinfo.model.date.LocalDateRange;

public class DatabaseLock {
    private String id;
    private String resourceId;
    private LocalDateRange lockRange;

    public DatabaseLock(String id, String resourceId, LocalDateRange lockRange) {
        this.id = id;
        this.resourceId = resourceId;
        this.lockRange = lockRange;
    }

    public String getId() {
        return id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LocalDateRange getLockRange() {
        return lockRange;
    }
}
