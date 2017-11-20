package org.activityinfo.ui.client.store.offline;

public class PendingEntry {
    private int id;
    private PendingTransaction transaction;

    public PendingEntry(int id, PendingTransaction transaction) {
        this.id = id;
        this.transaction = transaction;
    }

    public int getId() {
        return id;
    }

    public PendingTransaction getTransaction() {
        return transaction;
    }
}
