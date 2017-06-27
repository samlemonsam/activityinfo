package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.Window;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulValue;

/**
 * Listen for changes in the browser's online/offline status
 */
public class ConnectionListener {

    private final StatefulValue<Boolean> online = new StatefulValue<>(true);

    public void start() {
        start(online);
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
