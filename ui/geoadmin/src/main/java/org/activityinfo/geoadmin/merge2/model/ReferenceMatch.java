package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;

import java.util.Map;

/**
 * Explicit matching from a source lookup key to a referenced instance id.
 * 
 * <p>For example, when importing a new collection of Congolese <em>territoires</em>, the 
 * target administrative level for will have a 'province' reference field which links
 * the territoire to its enclosing province. To import the new territoire, we need the internal ActivityInfo 
 * id of its province, but the source will contain only the province name or perhaps the province name
 * and the province code.</p>
 * 
 * <p>In order to obtain the correct ActivityInfo id of the referenced province, we need to "look up" the province
 * id using one or more "key fields" in the source. The key fields in this example might include both the
 * province name and the province code. Together, these two names form a "lookup key."</p>
 * 
 * <p>Normally, the lookup key is matched automatically against the existing set of provinces to obtain the 
 * ActivityInfo id, but in some cases the user may need to override, confirm or correct the matching from the 
 * key to the ActivityInfo resourceId. In that case, the user adds an explicit {@code ReferenceMatch} to the 
 * model.</p>
 * 
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
