package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.*;

public class LocationTypeProxy implements DataProxy<ListLoadResult<LocationTypeEntry>> {

    private Dispatcher dispatcher;
    private int countryId;

    public LocationTypeProxy(Dispatcher dispatcher, int countryId) {
        this.dispatcher = dispatcher;
        this.countryId = countryId;
    }

    @Override
    public void load(DataReader<ListLoadResult<LocationTypeEntry>> reader, Object loadConfig, AsyncCallback<ListLoadResult<LocationTypeEntry>> callback) {
        dispatcher.execute(new GetSchema())
        .then(new Function<SchemaDTO, ListLoadResult<LocationTypeEntry>>() {
            @Override
            public ListLoadResult<LocationTypeEntry> apply(SchemaDTO schema) {

                // Build a dictionary of databases that have been shared with the user
                Map<Integer, String> databaseNames = new HashMap<>();
                for (UserDatabaseDTO db : schema.getDatabases()) {
                    databaseNames.put(db.getId(), db.getName());
                }

                List<LocationTypeEntry> list = new ArrayList<>();

                for (LocationTypeDTO locationType : schema.getCountryById(countryId).getLocationTypes()) {
                    if(!locationType.isDeleted()) {
                        if (locationType.getDatabaseId() == null) {
                            list.add(new LocationTypeEntry(locationType));
                        } else {
                            list.add(new LocationTypeEntry(locationType, databaseNames.get(locationType.getDatabaseId())));
                        }
                    }
                }

                Collections.sort(list);

                return new BaseListLoadResult<>(list);
            }
        })
        .then(callback);
    }


    public static ListStore<LocationTypeEntry> createStore(Dispatcher dispatcher, int countryId) {
        return new ListStore<>(new BaseListLoader<>(new LocationTypeProxy(dispatcher, countryId)));
    }
}
