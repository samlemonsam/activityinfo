package org.activityinfo.geoadmin.merge2.state;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

/**
 * Created by alex on 22-5-15.
 */
public interface ResourceStore {
    
    Observable<FormTree> getFormTree(ResourceId resourceId);

    Observable<ColumnSet> queryColumns(QueryModel queryModel);
}
