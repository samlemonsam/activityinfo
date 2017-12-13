package chdc.frontend.client.entry;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

public class DataEntryActivity implements Activity {

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Label());
    }

    @Override
    public void onCancel() {
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onStop() {

    }

}
