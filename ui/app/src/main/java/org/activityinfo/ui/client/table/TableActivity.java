package org.activityinfo.ui.client.table;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.data.FormService;

public class TableActivity extends AbstractActivity {

    private FormService formService;
    private TablePlace place;

    public TableActivity(FormService formService, TablePlace place) {
        this.formService = formService;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        TableModel tableModel = new TableModel(formService, place.getFormId());
        TableView view = new TableView(tableModel);
        panel.setWidget(view);
    }
}
