package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Map;


public class IndexedDBOfflineStore implements OfflineStore {

    private Map<ResourceId, FormClass> pendingQueue = new HashMap<>();

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

        switch (state) {
            // If we haven't succeeded in opening
            // the IndexedDB database, then put the form schema
            // in a holding pattern and start opening the database.
            case UNINITIALIZED:
                pendingQueue.put(formSchema.getId(), formSchema);
                startOpeningDatabase();
                break;

            // Still waiting on permissions or something...
            case LOADING:
                pendingQueue.put(formSchema.getId(), formSchema);
                break;

            // Database ready to go!
            case LOADED:
                indexedDB.putSchema(formSchema);
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

        // Store all pending form schemas then destroy the
        // pending queue
        for (FormClass schema : pendingQueue.values()) {
            indexedDB.putSchema(schema);
        }
        pendingQueue = null;
    }

}
