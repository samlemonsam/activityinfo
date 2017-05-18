package org.activityinfo.store.testing;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.NullFormScanCache;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.FormStorage;

public class TestingFormSource implements FormSource {


    private final TestingCatalog catalog;

    public TestingFormSource() {
        catalog = new TestingCatalog();
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(catalog);
        return Observable.just(builder.queryTree(formId));
    }

    @Override
    public Observable<FormRecord> getRecord(RecordRef recordRef) {
        Optional<FormStorage> form = catalog.getForm(recordRef.getFormId());
        Optional<FormRecord> record = form.get().get(recordRef.getRecordId());

        return Observable.just(record.get());
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache());
        return Observable.just(builder.build(queryModel));
    }
}
