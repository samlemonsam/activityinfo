package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;

import java.util.Map;

/**
 * User-defined matching from a lookup key to a target Resource id.
 */
public class ReferenceMatch {

    /**
     * Map from source field path to the lookup values.
     */
    private final Map<FieldPath, String> sourceValues;

    /**
     * The target form instance 
     */
    private final ResourceId targetId;


    public ReferenceMatch(Map<FieldPath, String> sourceValues, ResourceId targetId) {
        this.sourceValues = sourceValues;
        this.targetId = targetId;
    }

    public Map<FieldPath, String> getSourceValues() {
        return sourceValues;
    }

    public ResourceId getTargetId() {
        return targetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceMatch that = (ReferenceMatch) o;

        if (!sourceValues.equals(that.sourceValues)) return false;
        if (!targetId.equals(that.targetId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceValues.hashCode();
        result = 31 * result + targetId.hashCode();
        return result;
    }
}
