package org.activityinfo.ui.client.store;

/**
 * Shows the current offline status for a form.
 */
public class OfflineStatus {
    private boolean enabled;
    private boolean cached;

    public OfflineStatus(boolean enabled, boolean cached) {
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
