package org.activityinfo.service.store;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Records a change to the record
 */
public class RecordVersion {
    
    private ResourceId recordId;
    private long userId;
    private long time;
    private long version;
    private long formVersion;
    
    private RecordChangeType type;
    
    private final Map<ResourceId, FieldValue> values = new HashMap<>();

    /**
     * @return the id of the record changed.
     */
    public ResourceId getRecordId() {
        return recordId;
    }

    public void setRecordId(ResourceId recordId) {
        this.recordId = recordId;
    }

    /**
     * @return the id of the user changed.
     */
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * 
     * @return the time, in milliseconds since the epoch, of the change.
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the version that this change correspons to.
     */
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(long formVersion) {
        this.formVersion = formVersion;
    }

    /**
     * @return the values of the fields at this change.
     */
    public Map<ResourceId, FieldValue> getValues() {
        return values;
    }


    public RecordChangeType getType() {
        return type;
    }

    public void setType(RecordChangeType type) {
        this.type = type;
    }
}
