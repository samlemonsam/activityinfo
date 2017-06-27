package org.activityinfo.ui.client.store.offline;

/**
 * Describes the current state of the offline system
 */
public class OfflineStatus {

    private SnapshotStatus snapshot;
    private int pendingChangeCount;
    private int offlineFormCount;


    public OfflineStatus(SnapshotStatus snapshot, PendingStatus pendingStatus) {
        this.snapshot = snapshot;
        pendingChangeCount = pendingStatus.getCount();
    }

    public int getOfflineFormCount() {
        return offlineFormCount;
    }

    public int getPendingChangeCount() {
        return pendingChangeCount;
    }

    public boolean isSynced() {
        return pendingChangeCount == 0 && !snapshot.isEmpty();
    }
}
