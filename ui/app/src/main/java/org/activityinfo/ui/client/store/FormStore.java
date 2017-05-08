package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;

import java.util.List;

public interface FormStore {


    Observable<FormMetadata> getFormMetadata(ResourceId formId);

    Promise<Void> deleteForm(ResourceId formId);

    Observable<List<CatalogEntry>> getCatalogRoots();

    Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId);

    Observable<FormTree> getFormTree(ResourceId formId);

    Observable<ColumnSet> query(QueryModel queryModel);

    Observable<FormRecord> getRecord(RecordRef recordRef);
}
