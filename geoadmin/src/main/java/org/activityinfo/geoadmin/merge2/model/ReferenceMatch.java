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
    private final ResourceId targetInstanceId;


    public ReferenceMatch(Map<FieldPath, String> sourceValues, ResourceId targetInstanceId) {
        this.sourceValues = sourceValues;
        this.targetInstanceId = targetInstanceId;
    }

    public Map<FieldPath, String> getSourceValues() {
        return sourceValues;
    }

    public ResourceId getTargetInstanceId() {
        return targetInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceMatch that = (ReferenceMatch) o;

        if (!sourceValues.equals(that.sourceValues)) return false;
        if (!targetInstanceId.equals(that.targetInstanceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceValues.hashCode();
        result = 31 * result + targetInstanceId.hashCode();
        return result;
    }
}
