package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.List;


public class IndexedDBOfflineStore implements OfflineStore {

    private abstract static class PendingRequest<T> {
        public abstract void execute(IndexedDB indexedDB);
    }

    private List<PendingRequest> pendingRequests = new ArrayList<>();

    private enum State {
        UNINITIALIZED,
        LOADING,
        LOADED,
        ERROR
    }

    private State state = State.UNINITIALIZED;

    private IndexedDB indexedDB = null;

    @Override
    public void putSchema(FormClass formSchema) {
        execute(new PendingRequest() {
            @Override
            public void execute(IndexedDB indexedDB) {
                indexedDB.putSchema(formSchema);
            }
        });
    }

    @Override
    public void loadSchema(ResourceId formId, CallbackMaybe<FormClass> callback) {
        execute(new PendingRequest<FormClass>() {
            @Override
            public void execute(IndexedDB indexedDB) {
                indexedDB.loadSchema(formId, new IDBCallback<FormClass>() {
                    @Override
                    public void onSuccess(FormClass result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(JavaScriptObject error) {
                    }
                });
            }
        });
    }

    @Override
    public void enableOffline(ResourceId formId, boolean offline) {

    }

    private void execute(PendingRequest request) {

        switch (state) {
            // If we haven't succeeded in opening
            // the IndexedDB database, then put the form schema
            // in a holding pattern and start opening the database.
            case UNINITIALIZED:
                pendingRequests.add(request);
                startOpeningDatabase();
                break;

            // Still waiting on permissions or something...
            case LOADING:
                pendingRequests.add(request);
                break;

            // Database ready to go!
            case LOADED:
                request.execute(indexedDB);
                break;

            // Permission was refused, or the browser doesn't support or
            // something. Nothing to do.
            case ERROR:
                break;
        }
    }

    private void startOpeningDatabase() {
        // Transition our state to loading, start opening the database
        this.state = State.LOADING;

        IndexedDB.open(new IDBCallback<IndexedDB>() {
            @Override
            public void onSuccess(IndexedDB result) {
                // Expect to be LOADING
                assert state == State.LOADING : "Invalid state: " + state;
                onLoaded(result);
            }

            @Override
            public void onFailure(JavaScriptObject error) {
                // Expect to be LOADING
                // Transition to ERROR state
                assert state == State.LOADING : "Invalid state: " + state;
                state = State.ERROR;
            }
        });
    }

    private void onLoaded(IndexedDB result) {
        assert state == State.LOADING : "Invalid state: " + state;

        // Transition to LOADED!
        this.indexedDB = result;
        this.state = State.LOADED;

        // Execute all pending requests and then destory the pending
        // queue
        for (PendingRequest pendingRequest : pendingRequests) {
            pendingRequest.execute(indexedDB);
        }
        pendingRequests = null;
    }

}
