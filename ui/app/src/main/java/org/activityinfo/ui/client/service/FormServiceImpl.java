package org.activityinfo.ui.client.service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class FormServiceImpl implements FormService {

    private static final Logger LOGGER = Logger.getLogger(FormServiceImpl.class.getName());

    private ActivityInfoClientAsync client = new ActivityInfoClientAsyncImpl("http://localhost:8080/resources");

    private Map<ResourceId, ObservableForm> formMap = Maps.newHashMap();
    private List<PendingQuery> queries = new ArrayList<>();


    @Override
    public Observable<FormClass> getFormClass(ResourceId formId) {
        ObservableForm form = formMap.get(formId);
        if(form == null) {
            form = new ObservableForm(client, formId);
            formMap.put(formId, form);
        }
        return form;
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        // TODO: This is an incomplete implementation
        return getFormClass(formId).transform(new Function<FormClass, FormTree>() {
            @Override
            public FormTree apply(FormClass formSchema) {
                FormTree tree = new FormTree();
                for (FormField field : formSchema.getFields()) {
                    tree.addRootField(formSchema, field);
                }
                return tree;
            }
        });
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        return new ObservableQuery(client, queryModel);
    }
}
