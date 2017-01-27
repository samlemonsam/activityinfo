package org.activityinfo.ui.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.container.Viewport;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.ui.client.chrome.AppFrame;
import org.activityinfo.ui.client.http.HttpBus;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.store.FormStoreImpl;
import org.activityinfo.ui.client.table.TablePlace;

import java.util.logging.Logger;

/**
 * GWT EntryPoint that starts the application.
 */
public class AppEntryPoint implements EntryPoint {

    public static final TablePlace DEFAULT_PLACE = new TablePlace(CuidAdapter.activityFormClass(33));

    private static final Logger logger = Logger.getLogger(AppEntryPoint.class.getName());


    @Override
    public void onModuleLoad() {


        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus);

        ActivityInfoClientAsync client = new ActivityInfoClientAsyncImpl("http://localhost:8080/resources");
        HttpBus httpBus = new HttpBus(client);
        FormStore formStore = new FormStoreImpl(httpBus);

        Viewport viewport = new Viewport();
        AppFrame appFrame = new AppFrame(httpBus);

        ActivityMapper activityMapper = new AppActivityMapper(formStore);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appFrame.getDisplayWidget());

        AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, DEFAULT_PLACE);

        viewport.add(appFrame);

        RootLayoutPanel.get().add(viewport);

        historyHandler.handleCurrentHistory();
    }
}
