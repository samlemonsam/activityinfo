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
