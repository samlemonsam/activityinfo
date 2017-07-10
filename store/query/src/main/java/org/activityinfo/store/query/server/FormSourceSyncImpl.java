package org.activityinfo.store.query.server;

import com.google.common.base.Optional;
import org.activityinfo.model.analysis.Analysis;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.query.shared.FormScanCache;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.store.spi.FormStorage;

/**
 * Synchronous implementation of the {@link FormSource} interface, that
 * also ensures permissions are respected.
 */
public class FormSourceSyncImpl implements FormSource {

    private FormCatalog formCatalog;
    private int userId;
    private FormScanCache formScanCache;

    public FormSourceSyncImpl(FormCatalog formCatalog, FormScanCache cache, int userId) {
        this.formCatalog = formCatalog;
        this.userId = userId;
        this.formScanCache = cache;
    }

    public FormSourceSyncImpl(FormCatalog formCatalog, int userId) {
        this(formCatalog, new NullFormScanCache(), userId);
    }


    public FormMetadata getFormMetadata(ResourceId formId) {
        Optional<FormStorage> storage = formCatalog.getForm(formId);
        if(!storage.isPresent()) {
            return FormMetadata.notFound(formId);
        }
        FormPermissions permissions = storage.get().getPermissions(userId);
        if(!permissions.isVisible()) {
            return FormMetadata.forbidden(formId);
        }

        return FormMetadata.of(
            storage.get().cacheVersion(),
            storage.get().getFormClass(),
            permissions);
    }


    @Override
    public Observable<FormTree> getFormTree(ResourceId formId) {
        FormTreeBuilder builder = new FormTreeBuilder(new FormMetadataProvider() {
            @Override
            public FormMetadata getFormMetadata(ResourceId formId) {
                return FormSourceSyncImpl.this.getFormMetadata(formId);
            }
        });
        return Observable.just(builder.queryTree(formId));
    }

    @Override
    public Observable<Maybe<RecordTree>> getRecordTree(RecordRef recordRef) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef) {
        return Observable.loading();
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(formCatalog, formScanCache,
                new FormSupervisorAdapter(formCatalog, userId));
        return Observable.just(builder.build(queryModel));
    }

    @Override
    public Observable<Maybe<Analysis>> getAnalysis(String id) {
        throw new UnsupportedOperationException();
    }
}
