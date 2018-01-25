package chdc.frontend.client.dashboard;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DashboardActivity implements Activity {

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new DashboardWidget());
    }


    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String mayStop() {
        // This activity has no state that needs to be saved before
        // navigating away
        return null;
    }
}
