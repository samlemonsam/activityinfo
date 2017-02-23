package org.activityinfo.ui.client.input;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.activityinfo.ui.client.input.view.InputPanel;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Created by alex on 16-2-17.
 */
public class RecordActivity extends AbstractActivity {

    private FormStore formStore;
    private RecordPlace place;

    public RecordActivity(FormStore formStore, RecordPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        InputPanel inputPanel = new InputPanel(formStore, place.getFormId());

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading("Record");
        contentPanel.add(inputPanel);

        panel.setWidget(contentPanel);
    }
}
