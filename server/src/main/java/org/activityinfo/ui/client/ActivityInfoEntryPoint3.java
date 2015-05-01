package org.activityinfo.ui.client;

import com.google.common.base.Strings;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.remote.MergingDispatcher;
import org.activityinfo.legacy.client.remote.RemoteDispatcher;
import org.activityinfo.legacy.client.remote.cache.CacheManager;
import org.activityinfo.legacy.client.remote.cache.CachingDispatcher;
import org.activityinfo.legacy.client.remote.cache.SchemaCache;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.legacy.shared.auth.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.RemoteCommandService;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.ui.client.chrome.LeftPanel;
import org.activityinfo.ui.client.chrome.MainPanel;
import org.activityinfo.ui.client.inject.ClientSideAuthProvider;
import org.activityinfo.ui.client.page.instance.InstancePlace;
import org.activityinfo.ui.client.style.BaseStylesheet3;

/**
 * Entry Point for AI Version 3
 */
public class ActivityInfoEntryPoint3 implements EntryPoint {

    private EventBus eventBus = new LoggingEventBus();

    private ResourceLocator resourceLocator = createResourceLocator();

    private InstancePlace.Parser placeParser = new InstancePlace.Parser();

    @Override
    public void onModuleLoad() {

        Document.get().getBody().addClassName("bs");

        BaseStylesheet3.INSTANCE.ensureInjected();

        LeftPanel leftPanel = new LeftPanel();
        final MainPanel mainPanel = new MainPanel(resourceLocator);

        RootPanel root = RootPanel.get("root");
        root.add(leftPanel);
        root.add(mainPanel);

        startNavigation(mainPanel);
    }

    private void startNavigation(final MainPanel mainPanel) {


        mainPanel.navigate(parsePlace(Window.Location.getHash()));

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                InstancePlace place = parsePlace(event.getValue());
                mainPanel.navigate(place);
            }
        });
    }

    private InstancePlace parsePlace(String fragment) {
        fragment = Strings.nullToEmpty(fragment);
        if(fragment.startsWith("#")) {
            fragment = fragment.substring(1);
        }
        if(Strings.nullToEmpty(fragment).startsWith("i/")) {
            String token = fragment.substring("i/".length());
            return placeParser.parse(token);
        } else {
            return placeParser.parse("home");
        }
    }

    public ResourceLocator createResourceLocator() {

        RemoteCommandServiceAsync remoteService = GWT.create(RemoteCommandService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) remoteService;
        String moduleRelativeURL = "/ActivityInfo/cmd";
        endpoint.setServiceEntryPoint(moduleRelativeURL);

        CacheManager cacheManager = new CacheManager(eventBus);
        SchemaCache.initSource(cacheManager, new SchemaCache(cacheManager));

        AuthenticatedUser auth = new ClientSideAuthProvider().get();

        Dispatcher dispatcher = new CachingDispatcher(cacheManager,
                new MergingDispatcher(
                        new RemoteDispatcher(eventBus, auth, remoteService),
                        Scheduler.get()));

        return new ResourceLocatorAdaptor(dispatcher);
    }
}
