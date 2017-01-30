package org.activityinfo.ui.client.store;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A FormStore implementation that can be used for unit tests.
 */
public class TestingFormStore implements FormStore {

    public static final ResourceId SURVEY_FORM_ID = ResourceId.valueOf("FORM1");

    private Map<ResourceId, FormClass> formClassMap = new HashMap<>();

    public TestingFormStore() {
        FormClass surveyForm = new FormClass(SURVEY_FORM_ID);
        surveyForm.setLabel("Survey");
        surveyForm.addField(ResourceId.valueOf("FIELD1"))
                .setLabel("What is your name?")
                .setRequired(true)
                .setType(TextType.INSTANCE);


        formClassMap.put(surveyForm.getId(), surveyForm);
    }


    @Override
    public Observable<FormClass> getFormClass(ResourceId formId) {
        return Observable.just(formClassMap.get(formId));

    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogRoots() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        throw new UnsupportedOperationException("TODO");
    }
}