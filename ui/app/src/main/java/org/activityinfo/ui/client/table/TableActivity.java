package org.activityinfo.ui.client.table;

import com.google.common.base.Optional;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.storage.LocalStorage;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.view.TableView;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TableActivity extends AbstractActivity {

    private static final Logger LOGGER = Logger.getLogger(TableActivity.class.getName());

    private FormStore formStore;
    private TablePlace place;
    private TableView view;

    private LocalStorage storage;
    private Subscription modelSubscription;

    public TableActivity(FormStore formStore, LocalStorage storage, TablePlace place) {
        this.formStore = formStore;
        this.place = place;
        this.storage = storage;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        TableViewModel tableViewModel = new TableViewModel(formStore, initialModel(place.getFormId()));
        view = new TableView(formStore, tableViewModel);
        panel.setWidget(view);

        modelSubscription = view.getTableModel().subscribe(this::saveModel);
    }


    private String modelKey(ResourceId formId) {
        return "tableViewModel:" + formId.asString();
    }

    private TableModel initialModel(ResourceId formId) {

        Optional<JsonValue> object = storage.getObjectIfPresent(modelKey(formId));
        if(object.isPresent()) {
            try {
                return TableModel.fromJson(object.get().getAsJsonObject());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to deserialize saved model: ", e);
            }

        }
        return ImmutableTableModel
                .builder()
                .formId(formId)
                .build();
    }

    private void saveModel(Observable<TableModel> model) {
        if (model.isLoaded()) {
            storage.setObject(modelKey(place.getFormId()), model.get().toJson());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        view.stop();
        modelSubscription.unsubscribe();
    }
}
