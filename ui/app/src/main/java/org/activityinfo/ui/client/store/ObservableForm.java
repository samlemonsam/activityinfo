package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.http.FormMetadataRequest;
import org.activityinfo.ui.client.http.HttpBus;
import org.activityinfo.ui.client.http.HttpSubscription;

import java.util.logging.Logger;

/**
 * Maintains a cache of a FormSchema.
 *
 */
class ObservableForm extends Observable<FormMetadata> {

    private static final Logger LOGGER = Logger.getLogger(ObservableForm.class.getName());

    private final HttpBus httpBus;
    private OfflineStore offlineStore;
    private final ResourceId formId;

    /**
     * The schema version that we have loaded.
     */
    private long schemaVersion = -1;

    /**
     * True if this form is someone is listening for this
     * form.
     */
    private boolean connected = false;

    /**
     * Pending/completed fetch of this form's schema.
     */
    private FormClass schema = null;

    private FormMetadata metadata = null;

    private HttpSubscription httpSubscription = null;

    public ObservableForm(HttpBus httpBus, OfflineStore offlineStore, ResourceId formId) {
        this.httpBus = httpBus;
        this.offlineStore = offlineStore;
        this.formId = formId;
    }

    @Override
    protected void onConnect() {
        LOGGER.info(formId + ": Connected.");
        connected = true;

        if (schema == null) {
            LOGGER.info(formId + ": fetching...");

            // Try to load from the offline cache while starting a network request at the same time
            this.offlineStore.loadSchema(formId, new AsyncCallback<FormClass>() {
                @Override
                public void onFailure(Throwable caught) {

                }

                @Override
                public void onSuccess(FormClass result) {
                    cachedVersionLoaded(result);
                }
            });

            this.httpSubscription = httpBus.submit(new FormMetadataRequest(formId), new AsyncCallback<FormMetadata>() {
                @Override
                public void onFailure(Throwable caught) {
                    // TODO: handle deleted / no permission...
                }

                @Override
                public void onSuccess(FormMetadata result) {
                    newMetadataFetched(result);
                }

            });
        }
    }


    private void newMetadataFetched(FormMetadata result) {
        if (result.getSchemaVersion() > schemaVersion) {
            newSchemaVersionFetched(result.getSchema());
        }
        this.metadata = result;
        this.fireChange();
    }

    /**
     * A new version has been loaded from the network.
     */
    private void newSchemaVersionFetched(FormClass result) {

        LOGGER.info(formId + ": received version from network " + result.getSchemaVersion());

        this.schema = result;
        this.schemaVersion = result.getSchemaVersion();

        offlineStore.putSchema(result);
    }




    /**
     * A cached version has been loaded from the offline store
     */
    private void cachedVersionLoaded(FormClass cachedSchema) {
        if(cachedSchema.getSchemaVersion() > this.schemaVersion) {
            LOGGER.info(formId + ": Loaded version " + cachedSchema.getSchemaVersion() + " from offline store");
            this.schema = cachedSchema;
            this.schemaVersion = cachedSchema.getSchemaVersion();

            // TODO: this is not complete
            this.metadata = new FormMetadata();
            this.metadata.setId(this.formId);
            this.metadata.setVersion(cachedSchema.getSchemaVersion());
            this.metadata.setSchemaVersion(cachedSchema.getSchemaVersion());
            this.metadata.setSchema(cachedSchema);

            fireChange();
        }
    }


    @Override
    protected void onDisconnect() {
        LOGGER.info(formId + ": Disconnected.");
        connected = false;
        if (httpSubscription != null) {
            httpSubscription.cancel();
            httpSubscription = null;
        }
    }

    @Override
    public boolean isLoading() {
        return schema == null;
    }

    @Override
    public FormMetadata get() {
        assert !isLoading() : "loading: " + formId;
        return metadata;
    }
}
