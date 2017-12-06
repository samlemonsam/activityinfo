package chdc.frontend.client.table;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.view.TableView;

public class TableActivity implements Activity {


    private final FormStore formStore;

    public TableActivity(FormStore formStore) {
        this.formStore = formStore;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        TableModel model = ImmutableTableModel.builder()
                .formId(ResourceId.valueOf("incident"))
                .build();

        TableViewModel viewModel = new TableViewModel(formStore, model);

        TableView tableView = new TableView(formStore, viewModel);

        panel.setWidget(tableView);
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
