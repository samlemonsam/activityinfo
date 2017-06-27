package org.activityinfo.ui.client.chrome;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.http.HttpStatus;

/**
 * Displays the current connection status.
 */
public class ConnectionIndicator implements IsWidget {

    private InlineHTML html;

    public ConnectionIndicator(HttpBus bus) {
        html = new InlineHTML(ChromeBundle.BUNDLE.cloudIcon().getText());
        ChromeBundle.BUNDLE.cloudStyle().ensureInjected();

        bus.isOnline().subscribe(this::onOnlineStatusChanged);
        bus.getStatus().subscribe(this::onLoadingStatusChanged);
    }


    @Override
    public Widget asWidget() {
        return html;
    }


    private void onOnlineStatusChanged(Observable<Boolean> observable) {
        boolean connected = observable.isLoaded() && observable.get();

        toggleClass(!connected, ChromeBundle.BUNDLE.cloudStyle().offline());
    }


    private void onLoadingStatusChanged(Observable<HttpStatus> observable) {
        if (!observable.isLoading()) {
            switch (observable.get()) {
                case IDLE:
                    toggleClass(true, ChromeBundle.BUNDLE.cloudStyle().loading());
                    break;
                case FETCHING:
                case BROKEN:
                    toggleClass(false, ChromeBundle.BUNDLE.cloudStyle().loading());
                    break;
            }
        }
    }


    private void toggleClass(boolean add, String offline) {
        if(add) {
            html.getElement().addClassName(offline);
        } else {
            html.getElement().removeClassName(offline);
        }
    }
}
