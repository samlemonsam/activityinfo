package org.activityinfo.ui.client.store.offline;

/**
 * Shows the current offline status for a form.
 */
public class FormOfflineStatus {
    private boolean enabled;
    private boolean cached;

    public FormOfflineStatus(boolean enabled, boolean cached) {
        this.enabled = enabled;
        this.cached = enabled && cached;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCached() {
        return cached;
    }
}
