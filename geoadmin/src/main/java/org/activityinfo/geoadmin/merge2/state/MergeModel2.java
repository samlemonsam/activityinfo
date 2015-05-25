package org.activityinfo.geoadmin.merge2.state;

import com.google.common.collect.BiMap;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;

/**
 * Defines a process for merging the instances of a <em>source</em> form into
 * a <em>target</em> form.
 */
public class MergeModel2 {

    /**
     * The {@code ResourceId} of the collection that will ultimately be updated
     */
    private ResourceId targetFormId;

    /**
     * The {@code ResourceId} of the collection that is to be merged into the <em>target</em>
     */
    private ResourceId sourceFormId;
    
    
    public ResourceId getSourceFormId() {
        return sourceFormId;
    }

    public void setSourceFormId(ResourceId sourceFormId) {
        this.sourceFormId = sourceFormId;
    }

    public ResourceId getTargetFormId() {
        return targetFormId;
    }

    public void setTargetFormId(ResourceId targetFormId) {
        this.targetFormId = targetFormId;
    }
}
