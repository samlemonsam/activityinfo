package org.activityinfo.ui.client.store.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;

import java.util.Optional;

public class CachedRecord extends Observable<FormRecord> {

    private final RecordRef recordRef;
    private final IDBExecutor executor;
    private FormRecord record = null;


    public CachedRecord(RecordRef recordRef, IDBExecutor executor) {
        this.recordRef = recordRef;
        this.executor = executor;
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        executor.begin(RecordStore.NAME).query(tx -> tx.records().get(recordRef)).then(new AsyncCallback<Optional<FormRecord>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(Optional<FormRecord> result) {
                if(result.isPresent()) {
                    record = result.get();
                    fireChange();
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
    }

    @Override
    public boolean isLoading() {
        return record == null;
    }

    @Override
    public FormRecord get() {
        assert record != null : "not loaded";
        return record;
    }
}
