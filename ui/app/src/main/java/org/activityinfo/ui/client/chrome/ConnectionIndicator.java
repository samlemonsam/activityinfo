package org.activityinfo.ui.client.chrome;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.http.HttpBus;

/**
 * Displays the current connection status.
 */
public class ConnectionIndicator implements IsWidget {

    private InlineHTML html;

    public ConnectionIndicator(HttpBus bus) {
        html = new InlineHTML(ChromeBundle.BUNDLE.cloudIcon().getText());
        ChromeBundle.BUNDLE.cloudStyle().ensureInjected();

        bus.getFetchingStatus().subscribe(this::onFetchingStatusChanged);
        bus.getOnline().subscribe(this::onOnlineStatusChanged);
    }


    @Override
    public Widget asWidget() {
        return html;
    }


    private void onOnlineStatusChanged(Observable<Boolean> observable) {
        boolean connected = observable.isLoaded() && observable.get();

        toggleClass(ChromeBundle.BUNDLE.cloudStyle().offline(), !connected);
    }

    private void onFetchingStatusChanged(Observable<Boolean> fetching) {
        toggleClass(ChromeBundle.BUNDLE.cloudStyle().loading(),
            fetching.isLoaded() && fetching.get());
    }


    private void toggleClass(String offline, boolean add) {
        if(add) {
            html.getElement().addClassName(offline);
        } else {
            html.getElement().removeClassName(offline);
        }
    }
}
