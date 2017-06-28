package org.activityinfo.ui.client.input;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.activityinfo.ui.client.input.view.FormInputView;
import org.activityinfo.ui.client.store.FormStore;

public class RecordActivity extends AbstractActivity {

    private FormStore formStore;
    private RecordPlace place;

    public RecordActivity(FormStore formStore, RecordPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        FormInputView formInputView = new FormInputView(formStore, place.getRecordRef());

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("Record");
        contentPanel.add(formInputView);

        panel.setWidget(contentPanel);
    }
}
