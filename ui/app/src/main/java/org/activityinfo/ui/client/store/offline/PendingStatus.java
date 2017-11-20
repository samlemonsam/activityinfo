package org.activityinfo.ui.client.store.offline;

import org.activityinfo.model.resource.ResourceId;

import java.util.Set;

/**
 * Provides information on the status of pending entries
 */
public class PendingStatus {

    /**
     * The ids of forms with pending updates in the pending queue.
     */
    private Set<ResourceId> forms;
    private int count;

    public PendingStatus(int count, Set<ResourceId> forms) {
        this.count = count;
        this.forms = forms;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public Set<ResourceId> getForms() {
        return forms;
    }

    /**
     * @return the total number of pending transactions.
     */
    public int getCount() {
        return count;
    }
}
