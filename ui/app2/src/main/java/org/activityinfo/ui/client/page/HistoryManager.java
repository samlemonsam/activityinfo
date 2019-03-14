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
package org.activityinfo.ui.client.page;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.ui.client.AppEvents;
import org.activityinfo.ui.client.ClientContext;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.config.DbListPageState;
import org.activityinfo.ui.client.page.dashboard.DashboardPlace;

/**
 * NOTE: The PlaceManager must be invoked after PageManager
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
@Singleton
public class HistoryManager {

    private final EventBus eventBus;
    private final PageStateSerializer placeSerializer;

    @Inject
    public HistoryManager(EventBus eventBus, PageStateSerializer placeSerializer) {
        this.placeSerializer = placeSerializer;
        this.eventBus = eventBus;

        this.eventBus.addListener(AppEvents.INIT, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                fireInitialPage();
            }
        });

        this.eventBus.addListener(NavigationHandler.NAVIGATION_AGREED,
                new Listener<NavigationEvent>() {
                    @Override
                    public void handleEvent(NavigationEvent be) {
                        onNavigationCompleted(be.getPlace());
                    }
                });

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onBrowserMovement(event.getValue());
            }
        });

        Log.trace("HistoryManager plugged in");
    }

    private void fireInitialPage() {

        if (History.getToken().length() != 0) {
            Log.debug("HistoryManager: firing initial placed based on intial token: "
                    + History.getToken());
            History.fireCurrentHistoryState();

        } else {
            eventBus.fireEvent(new NavigationEvent(
                    NavigationHandler.NAVIGATION_REQUESTED, defaultPlace()));
        }
    }

    private PageState defaultPlace() {
        if(ClientContext.isV4Enabled()) {
            return new DbListPageState();
        } else {
            return new DashboardPlace();
        }
    }

    private void onNavigationCompleted(PageState place) {

        String token = PageStateSerializer.serialize(place);

        /*
         * If it's a duplicate, we're not totally interested
         */
        if (!token.equals(History.getToken())) {

            /*
             * Lodge in the browser's history
             */
            History.newItem(token, false);

        }
    }

    private void onBrowserMovement(String token) {

        Log.debug("HistoryManager: Browser movement observed (" + token
                + "), firing NavigationRequested");

        PageState place = placeSerializer.deserialize(token);
        if (place != null) {

            eventBus.fireEvent(new NavigationEvent(
                    NavigationHandler.NAVIGATION_REQUESTED, place));

        } else {
            Log.debug("HistoryManager: Could not deserialize '" + token
                    + "', no action taken.");
        }
    }

}
