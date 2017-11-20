package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.Window;
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
