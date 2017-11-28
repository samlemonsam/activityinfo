package chdc.frontend.client;

import chdc.frontend.client.table.TablePlace;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sencha.gxt.widget.core.client.container.Viewport;

/**
 * This is the entry point for the Single Page Application (SPA).
 */
public class ChdcEntryPoint implements EntryPoint {
    private static final Place DEFAULT_PLACE = new TablePlace();

    @Override
    public void onModuleLoad() {

        ChdcResources.RESOURCES.getStyle().ensureInjected();

        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus);

        Viewport viewport = new Viewport();
        ChdcFrame appFrame = new ChdcFrame();

        ActivityMapper activityMapper = new ChdcActivityMapper();
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appFrame.getDisplayWidget());

        ChdcPlaceHistoryMapper historyMapper = new ChdcPlaceHistoryMapper();
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, DEFAULT_PLACE);

        viewport.add(appFrame);

        RootLayoutPanel.get().add(viewport);

        historyHandler.handleCurrentHistory();
    }
}
