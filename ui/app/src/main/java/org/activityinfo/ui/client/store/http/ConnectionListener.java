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
package org.activityinfo.ui.client.store.http;

import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listen for changes in the browser's online/offline status
 */
public class ConnectionListener {

    private static final Logger LOGGER = Logger.getLogger(ConnectionListener.class.getName());

    private final StatefulValue<Boolean> online = new StatefulValue<>(true);

    public void start() {
        try {
            start(online);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not start connection listener", e);
        }
    }


    private static native void start(StatefulValue<Boolean> connection) /*-{
        $wnd.addEventListener('online', function(event) {
            connection.@StatefulValue::updateIfNotEqual(*)(true);
        });
        $wnd.addEventListener('offline', function(event) {
            connection.@StatefulValue::updateIfNotEqual(*)(false);
        });

    }-*/;

    public Observable<Boolean> getOnline() {
        return online;
    }
}
