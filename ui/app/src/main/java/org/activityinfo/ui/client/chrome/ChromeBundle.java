package org.activityinfo.ui.client.chrome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

/**
 * CSS and images for application chrome
 */
public interface ChromeBundle extends ClientBundle {

    ChromeBundle BUNDLE = GWT.create(ChromeBundle.class);

    @Source("cloud.svg")
    TextResource cloudIcon();

    @Source("offline.svg")
    TextResource offlineIcon();

    @Source("logo.html")
    TextResource logoLink();

    @Source({ "body.gss", "cloud.gss", "locale.gss", "offline.gss"})
    Style style();

    interface Style extends CssResource {
        String online();
        String offline();
        String fetching();

        @ClassName("offline-bar")
        String offlineBar();

        @ClassName("cloud-bg")
        String cloudBg();

        String localeIcon();

        @ClassName("fetching-icon")
        String fetchingIcon();

        String pending();

        @ClassName("pending-icon")
        String pendingIcon();

        String appBarButton();

        String synced();

        @ClassName("sync-check")
        String syncCheck();

        @ClassName("sync-icon")
        String syncIcon();

        String appTitle();

        @ClassName("sync-circle")
        String syncCircle();

        String appBar();
    }

}
