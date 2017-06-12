package org.activityinfo.ui.client.chrome;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.store.http.HttpStatus;

/**
 * Displays the current connection status.
 */
public class ConnectionIndicator implements IsWidget {

    private HTML html = new HTML();


    @Override
    public Widget asWidget() {
        return html;
    }

    public void setStatus(Observable<HttpStatus> status) {
        status.subscribe(new Observer<HttpStatus>() {
            @Override
            public void onChange(Observable<HttpStatus> observable) {
                if (!observable.isLoading()) {
                    switch (observable.get()) {
                        case IDLE:
                            html.setText("Idle.");
                            break;
                        case FETCHING:
                            html.setText("Fetching...");
                            break;
                        case BROKEN:
                            html.setText("Connection problem.");
                            break;
                    }
                }
            }
        });
    }
}
