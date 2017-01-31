package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import java.util.List;

public interface FormStore {


    Observable<FormClass> getFormClass(ResourceId formId);

    Observable<List<CatalogEntry>> getCatalogRoots();

    Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId);

    Observable<FormTree> getFormTree(ResourceId formId);

    Observable<ColumnSet> query(QueryModel queryModel);
}
