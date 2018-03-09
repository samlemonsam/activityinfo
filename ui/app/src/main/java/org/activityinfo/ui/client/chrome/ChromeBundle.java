/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Source("settings.svg")
    TextResource settingsIcon();

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
