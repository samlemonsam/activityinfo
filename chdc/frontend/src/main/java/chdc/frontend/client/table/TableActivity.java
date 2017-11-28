package chdc.frontend.client.table;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class TableActivity implements Activity {


    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new IncidentGrid());
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public String mayStop() {
        return null;
    }
}
