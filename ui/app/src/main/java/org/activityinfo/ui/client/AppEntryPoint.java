package org.activityinfo.ui.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.container.Viewport;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.indexedb.IDBFactoryImpl;
import org.activityinfo.ui.client.analysis.AnalysisPlace;
import org.activityinfo.ui.client.chrome.AppFrame;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.store.http.ConnectionListener;
import org.activityinfo.ui.client.store.offline.RecordSynchronizer;
import org.activityinfo.ui.client.store.http.HttpBus;
import org.activityinfo.ui.client.store.offline.OfflineStore;

import java.util.logging.Logger;

/**
 * GWT EntryPoint that starts the application.
 */
public class AppEntryPoint implements EntryPoint {

    //public static final TablePlace DEFAULT_PLACE = new TablePlace(CuidAdapter.activityFormClass(33));
    public static final Place DEFAULT_PLACE = new AnalysisPlace();

    private static final Logger logger = Logger.getLogger(AppEntryPoint.class.getName());


    @Override
    public void onModuleLoad() {


        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus);

        ConnectionListener connectionListener = new ConnectionListener();
        connectionListener.start();

        ActivityInfoClientAsync client = new ActivityInfoClientAsyncImpl(findServerUrl());
        HttpBus httpBus = new HttpBus(connectionListener.getOnline(), client);

        OfflineStore offlineStore = new OfflineStore(httpBus, IDBFactoryImpl.create());

        FormStore formStore = new FormStoreImpl(httpBus, offlineStore, Scheduler.get());

        Viewport viewport = new Viewport();
        AppFrame appFrame = new AppFrame(httpBus);

        ActivityMapper activityMapper = new AppActivityMapper(formStore);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appFrame.getDisplayWidget());

        AppPlaceHistoryMapper historyMapper = new AppPlaceHistoryMapper();
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, DEFAULT_PLACE);

        // Start synchronizer...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpBus, offlineStore);

        viewport.add(appFrame);

        RootLayoutPanel.get().add(viewport);

        historyHandler.handleCurrentHistory();
    }

    private String findServerUrl() {
        if (Window.Location.getHostName().equals("localhost")) {
            return "http://localhost:8080/resources";
        } else {
            return "/resources";
        }
    }
}
