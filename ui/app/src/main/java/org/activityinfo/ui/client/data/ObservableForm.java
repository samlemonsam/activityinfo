package org.activityinfo.ui.client.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;

import java.util.logging.Logger;

/**
 * Maintains a cache of a FormSchema.
 *
 */
class ObservableForm extends Observable<FormClass> {

    private static final Logger LOGGER = Logger.getLogger(ObservableForm.class.getName());

    private final ActivityInfoClientAsync client;
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
    private Promise<FormClass> formSchema = null;

    public ObservableForm(ActivityInfoClientAsync client, ResourceId formId) {
        this.client = client;
        this.formId = formId;
    }

    @Override
    protected void onConnect() {
        LOGGER.info(formId + ": Connected.");
        connected = true;

        if (formSchema == null) {
            LOGGER.info(formId + ": fetching...");
            formSchema = client.getFormSchema(formId.asString());
            formSchema.then(new AsyncCallback<FormClass>() {
                @Override
                public void onFailure(Throwable caught) {
                    // TODO: schedule retrying.
                }

                @Override
                public void onSuccess(FormClass result) {
                    LOGGER.info(formId + ": received version " + result.getSchemaVersion());
                    ObservableForm.this.schemaVersion = result.getSchemaVersion();
                    ObservableForm.this.fireChange();
                }
            });
        }
    }

    @Override
    protected void onDisconnect() {
        LOGGER.info(formId + ": Disconnected.");
        connected = false;
    }

    @Override
    public boolean isLoading() {
        if (formSchema == null) {
            return true;
        }
        return formSchema.getState() != Promise.State.FULFILLED;
    }

    @Override
    public FormClass get() {
        assert !isLoading() : "loading: " + formId;
        return formSchema.get();
    }
}
