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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpBus;

public class ConnectionIcon implements IsWidget {

    private HTML icon;

    public ConnectionIcon(HttpBus bus) {

        icon = new InlineHTML(ChromeBundle.BUNDLE.cloudIcon().getText());

        bus.getFetchingStatus().subscribe(this::onFetchingStatusChanged);
        bus.getOnline().subscribe(this::onOnlineStatusChanged);
    }

    private void onOnlineStatusChanged(Observable<Boolean> observable) {
        boolean connected = observable.isLoaded() && observable.get();

        toggleClass(ChromeBundle.BUNDLE.style().offline(), !connected);
    }

    private void onFetchingStatusChanged(Observable<Boolean> fetching) {
        toggleClass(ChromeBundle.BUNDLE.style().fetching(),
            fetching.isLoaded() && fetching.get());
    }


    private void toggleClass(String offline, boolean add) {
        if(add) {
            icon.getElement().addClassName(offline);
        } else {
            icon.getElement().removeClassName(offline);
        }
    }

    @Override
    public Widget asWidget() {
        return icon;
    }
}
