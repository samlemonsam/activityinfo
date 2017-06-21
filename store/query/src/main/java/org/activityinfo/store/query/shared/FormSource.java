package org.activityinfo.store.query.shared;

import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;

/**
 * Source of form data and metadata for analysis components
 */
public interface FormSource {

    Observable<FormTree> getFormTree(ResourceId formId);

    Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef);

    Observable<ColumnSet> query(QueryModel queryModel);

}
