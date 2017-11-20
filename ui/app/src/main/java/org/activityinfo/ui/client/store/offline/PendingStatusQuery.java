package org.activityinfo.ui.client.store.offline;

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

public class PendingStatusQuery extends SimpleTask<PendingStatus> {
    private OfflineDatabase database;

    public PendingStatusQuery(OfflineDatabase database) {
        this.database = database;
    }

    @Override
    protected Promise<PendingStatus> execute() {
        return database
            .begin(PendingStore.DEF)
            .query(tx -> tx.objectStore(PendingStore.DEF).getStatus());
    }
}