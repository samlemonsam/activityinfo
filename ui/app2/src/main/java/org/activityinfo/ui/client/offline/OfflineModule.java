package org.activityinfo.ui.client.offline;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.remote.Remote;

public class OfflineModule extends AbstractGinModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public OfflineController provideOfflineController(EventBus eventBus, @Remote Dispatcher remoteDispatcher, AuthenticatedUser user) {
        if(System.getProperty("user.agent").equals("safari")) {
            return new OfflineControllerWebkit(eventBus, remoteDispatcher, user);
        } else {
            return new OfflineControllerUnsupported(remoteDispatcher);
        }
    }
}
