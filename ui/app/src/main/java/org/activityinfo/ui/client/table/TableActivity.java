package org.activityinfo.ui.client.table;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.model.ImmutableTableModel;
import org.activityinfo.ui.client.table.view.TableView;
import org.activityinfo.ui.client.table.viewModel.TableViewModel;

public class TableActivity extends AbstractActivity {

    private FormStore formStore;
    private TablePlace place;

    public TableActivity(FormStore formStore, TablePlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        ImmutableTableModel tableModel = ImmutableTableModel
                .builder()
                .formId(place.getFormId())
                .build();

        TableViewModel tableViewModel = new TableViewModel(formStore, tableModel);
        TableView view = new TableView(tableViewModel);
        panel.setWidget(view);
    }
}
