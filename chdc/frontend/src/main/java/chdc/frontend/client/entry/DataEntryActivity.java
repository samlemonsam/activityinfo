package chdc.frontend.client.entry;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.store.FormStore;

public class DataEntryActivity implements Activity {

    private final FormStore formStore;
    private final DataEntryPlace place;

    public DataEntryActivity(FormStore formStore, DataEntryPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new DataEntryWidget(formStore, place.getRecordRef()));
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
