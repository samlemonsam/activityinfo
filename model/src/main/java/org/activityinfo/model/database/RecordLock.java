package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateInterval;

public class RecordLock implements JsonSerializable {
    private ResourceId id;
    private String label;
    private ResourceId resourceId;
    private LocalDateInterval dateRange;

    private RecordLock() {}

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
    public LocalDateInterval getDateRange() {
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

    public static RecordLock fromJson(JsonValue object) {
        RecordLock lock = new RecordLock();
        lock.id = ResourceId.valueOf(object.getString("id"));
        lock.label = object.getString("label");
        lock.resourceId = ResourceId.valueOf(object.getString("resourceId"));
        lock.dateRange = LocalDateInterval.fromJson(object.get("dateRange"));

        return lock;
    }

    public static class Builder {

        private RecordLock lock = new RecordLock();

        public Builder setId(ResourceId id) {
            lock.id = id;
            return this;
        }

        public Builder setResourceId(ResourceId resourceId) {
            lock.resourceId = resourceId;
            return this;
        }

        public Builder setDateRange(LocalDateInterval range) {
            lock.dateRange = range;
            return this;
        }

        public Builder setLabel(String name) {
            lock.label = name;
            return this;
        }

        public RecordLock build() {
            assert lock.id != null : "id is missing";
            assert lock.resourceId != null : "resourceId is missing";
            assert lock.dateRange != null : "dateRange is missing";
            assert lock.label != null : "label is missing";
            return lock;
        }
    }
}
