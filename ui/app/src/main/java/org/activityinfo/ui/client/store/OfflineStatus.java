package org.activityinfo.ui.client.store;

/**
 * Shows the current offline status for a form.
 */
public class OfflineStatus {
    private boolean enabled;

    public OfflineStatus(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
