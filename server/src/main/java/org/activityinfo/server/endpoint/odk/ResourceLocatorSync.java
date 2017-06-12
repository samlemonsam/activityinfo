package org.activityinfo.server.endpoint.odk;

import com.google.inject.ImplementedBy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ResourceLocatorSyncImpl.class)
public interface ResourceLocatorSync extends FormClassProvider {
    
    void persist(FormInstance formInstance);

    FormClass getFormClass(ResourceId formId);

    FormTree getFormTree(ResourceId formId);

    List<ReferenceChoice> getReferenceChoices(Collection<ResourceId> range);

    void persist(FormClass formClass);

    ColumnSet query(QueryModel model);
}
