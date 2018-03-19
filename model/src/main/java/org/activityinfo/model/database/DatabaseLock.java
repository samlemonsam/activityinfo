package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.date.LocalDateRange;
import org.activityinfo.model.resource.ResourceId;

public class DatabaseLock implements JsonSerializable {
    private ResourceId id;
    private String label;
    private ResourceId resourceId;
    private LocalDateRange dateRange;
    private String filter;

    private DatabaseLock() {}

    /**
     * @return the id of this lock itself.
     */
    public ResourceId getId() {
        return id;
    }

    /**
     * @return the name of this lock.
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @return the id of the resource to be locked.
     */
    public ResourceId getResourceId() {
        return resourceId;
    }

    /**
     * @return the date range of records to which this lock applies.
     */
    public LocalDateRange getDateRange() {
        return dateRange;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", id.asString());
        object.put("label", label);
        object.put("resourceId", resourceId.asString());
        object.put("dateRange", dateRange.toJson());
        return object;
    }

    public static class Builder {

        private DatabaseLock lock = new DatabaseLock();

        public Builder setId(ResourceId id) {
            lock.id = id;
            return this;
        }

        public Builder setResourceId(ResourceId resourceId) {
            lock.resourceId = resourceId;
            return this;
        }

        public Builder setDateRange(LocalDateRange range) {
            lock.dateRange = range;
            return this;
        }

        public Builder setLabel(String name) {
            lock.label = name;
            return this;
        }

        public DatabaseLock build() {
            assert lock.id != null : "id is missing";
            assert lock.resourceId != null : "resourceId is missing";
            assert lock.dateRange != null : "dateRange is missing";
            assert lock.label != null : "label is missing";
            return lock;
        }
    }
}
