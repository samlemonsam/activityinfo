package org.activityinfo.geoadmin.merge;

import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;


public class MergeViewBuilder {
    private final ActivityInfoClient client;

    public MergeViewBuilder(ActivityInfoClient client) {
        this.client = client;
    }
    
    public void build(ResourceId targetForm) {
        FormTree formTree = client.getFormTree(targetForm);
        
    }
}
