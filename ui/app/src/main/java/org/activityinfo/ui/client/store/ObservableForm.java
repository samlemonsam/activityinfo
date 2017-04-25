package org.activityinfo.ui.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.http.FormSchemaRequest;
import org.activityinfo.ui.client.http.HttpBus;
import org.activityinfo.ui.client.http.HttpSubscription;

import java.util.logging.Logger;

/**
 * Maintains a cache of a FormSchema.
 *
 */
class ObservableForm extends Observable<FormClass> {

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
    private FormClass formSchema = null;

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

        if (formSchema == null) {
            LOGGER.info(formId + ": fetching...");

            this.httpSubscription = httpBus.submit(new FormSchemaRequest(formId), new AsyncCallback<FormClass>() {
                @Override
                public void onFailure(Throwable caught) {
                    // TODO: handle deleted / no permission...
                }

                @Override
                public void onSuccess(FormClass result) {
                    LOGGER.info(formId + ": received version " + result.getSchemaVersion());
                    if (result.getSchemaVersion() > schemaVersion) {
                        newVersionFetched(result);
                    }
                }
            });
        }
    }

    /**
     * A new version has been loaded from the network.
     */
    private void newVersionFetched(FormClass result) {
        formSchema = result;

        offlineStore.putSchema(result);

        ObservableForm.this.schemaVersion = result.getSchemaVersion();
        ObservableForm.this.fireChange();
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
        return formSchema == null;
    }

    @Override
    public FormClass get() {
        assert !isLoading() : "loading: " + formId;
        return formSchema;
    }
}
