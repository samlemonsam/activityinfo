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

    @Source("logo.html")
    TextResource logoLink();

    @Source("cloud.gss")
    CloudStyle cloudStyle();

    interface CloudStyle extends CssResource {
        String syncing();
        String online();
        String offline();
        String loading();

        @ClassName("offline-bar")
        String offlineBar();

        @ClassName("sync-circle")
        String syncCircle();

        @ClassName("cloud-bg")
        String cloudBg();

        @ClassName("sync-icon")
        String syncIcon();
    }

}
