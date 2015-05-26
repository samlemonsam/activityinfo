package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.resource.ResourceId;

/**
 * Matches a Resource in the source collection to an existing resource in the target collection
 */
public class InstanceMatch {
    
    private ResourceId sourceId;
    private ResourceId targetId;

    public InstanceMatch(ResourceId sourceId, ResourceId targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public ResourceId getSourceId() {
        return sourceId;
    }

    public ResourceId getTargetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstanceMatch that = (InstanceMatch) o;

        if (!sourceId.equals(that.sourceId)) return false;
        if (!targetId.equals(that.targetId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId.hashCode();
        result = 31 * result + targetId.hashCode();
        return result;
    }
}
