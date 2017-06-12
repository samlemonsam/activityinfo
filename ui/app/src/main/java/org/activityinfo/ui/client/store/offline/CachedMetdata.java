package org.activityinfo.ui.client.store.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CachedMetdata extends Observable<FormMetadata> {

    private static final Logger LOGGER = Logger.getLogger(CachedMetdata.class.getName());

    private ResourceId formId;
    private IDBExecutor executor;
    private FormMetadata metadata = null;

    public CachedMetdata(IDBExecutor executor, ResourceId formId) {
        this.formId = formId;
        this.executor = executor;

        this.executor.begin(SchemaStore.NAME)
                .query(tx -> tx.schemas().get(formId))
                .then(new AsyncCallback<Optional<FormClass>>() {
            @Override
            public void onFailure(Throwable caught) {
                LOGGER.log(Level.SEVERE, "Failed to retrieve " + formId + " schema from offline store", caught);
            }

            @Override
            public void onSuccess(Optional<FormClass> result) {
                if(result.isPresent()) {
                    metadata = new FormMetadata();
                    metadata.setId(formId);
                    metadata.setVersion(result.get().getSchemaVersion());
                    metadata.setSchema(result.get());
                    fireChange();
                }
            }
        });
    }

    @Override
    public boolean isLoading() {
        return metadata == null;
    }

    @Override
    public FormMetadata get() {
        assert metadata != null;
        return metadata;
    }
}
