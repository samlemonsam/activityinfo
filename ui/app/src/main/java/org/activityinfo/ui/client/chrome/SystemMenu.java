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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.AppCache;

/**
 * System menu with application options, etc
 */
public class SystemMenu implements IsWidget {

    private final InlineHTML icon;
    private final MenuItem appCacheStatusItem;

    public SystemMenu(AppCache appCache) {

        MenuItem versionItem = new MenuItem();
        versionItem.setText(I18N.CONSTANTS.appVersion() + " " + appCache.getCurrentVersion());
        versionItem.setHideOnClick(false);

        appCacheStatusItem = new MenuItem();
        appCacheStatusItem.addSelectionHandler(this::loadUpdatedAppCache);

        MenuItem logoutItem = new MenuItem();
        logoutItem.setText(I18N.CONSTANTS.logout());
        logoutItem.addSelectionHandler(this::logout);

        Menu menu = new Menu();
        menu.add(versionItem);
        menu.add(appCacheStatusItem);
        menu.add(new SeparatorMenuItem());
        menu.add(logoutItem);

        icon = new InlineHTML(ChromeBundle.BUNDLE.settingsIcon().getText());
        icon.addStyleName(ChromeBundle.BUNDLE.style().appBarButton());
        icon.addClickHandler(event ->
            menu.show(icon.getElement(), new Style.AnchorAlignment(Style.Anchor.TOP, Style.Anchor.BOTTOM)));

        icon.addClickHandler(event -> appCache.checkForUpdates());

        appCache.getStatus().subscribe(this::updateAppCacheView);
    }


    @Override
    public Widget asWidget() {
        return icon;
    }

    private String statusMessage(AppCache.Status status) {
        switch (status) {
            default:
            case UNCACHED:
            case OBSOLETE:
            case IDLE:
                return I18N.CONSTANTS.versionLatest();
            case DOWNLOADING:
            case CHECKING:
                return I18N.CONSTANTS.versionChecking();
            case UPDATE_READY:
                return I18N.CONSTANTS.versionUpdateAvailable();
        }
    }

    private void updateAppCacheView(Observable<AppCache.Status> status) {
        appCacheStatusItem.setText(statusMessage(status.get()));
        appCacheStatusItem.setEnabled(status.get() == AppCache.Status.UPDATE_READY);
    }

    private void loadUpdatedAppCache(SelectionEvent<Item> event) {
        Window.Location.reload();
    }

    private void logout(SelectionEvent<Item> event) {
        Window.Location.assign("/logout");
    }
}
