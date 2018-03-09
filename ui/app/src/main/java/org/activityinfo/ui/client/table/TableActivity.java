/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
                return TableModel.fromJson(object.get());
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
