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
package org.activityinfo.ui.client.inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.ui.client.AppCacheMonitor;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.LoggingEventBus;
import org.activityinfo.ui.client.dispatch.DispatchEventSource;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.ResourceLocatorAdaptor;
import org.activityinfo.ui.client.dispatch.remote.MergingDispatcher;
import org.activityinfo.ui.client.dispatch.remote.Remote;
import org.activityinfo.ui.client.dispatch.remote.RemoteDispatcher;
import org.activityinfo.ui.client.dispatch.remote.cache.CacheManager;
import org.activityinfo.ui.client.dispatch.remote.cache.CachingDispatcher;
import org.activityinfo.ui.client.dispatch.state.GxtStateProvider;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.local.LocalController;
import org.activityinfo.ui.client.page.Frame;
import org.activityinfo.ui.client.page.PageStateSerializer;
import org.activityinfo.ui.client.page.app.AppFrameSet;
import org.activityinfo.ui.client.page.common.GalleryPage;
import org.activityinfo.ui.client.page.common.GalleryView;

public class AppModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(AuthenticatedUser.class).toProvider(ClientSideAuthProvider.class);
        bind(RemoteCommandServiceAsync.class).toProvider(RemoteServiceProvider.class).in(Singleton.class);
        bind(Dispatcher.class).annotatedWith(Remote.class).to(RemoteDispatcher.class).in(Singleton.class);
        bind(DispatchEventSource.class).to(CacheManager.class);
        bind(PageStateSerializer.class).in(Singleton.class);
        bind(EventBus.class).to(LoggingEventBus.class).in(Singleton.class);

        bind(StateProvider.class).to(GxtStateProvider.class);
        bind(Frame.class).annotatedWith(Root.class).to(AppFrameSet.class);
        bind(GalleryView.class).to(GalleryPage.class);

    }

    @Provides
    public Dispatcher provideDispatcher(CacheManager proxyManager, LocalController controller) {
        return new CachingDispatcher(proxyManager, new MergingDispatcher(controller, Scheduler.get()));
    }

    @Provides @Singleton
    public ResourceLocator provideResourceLocator(Dispatcher dispatcher) {
        return new ResourceLocatorAdaptor();
    }
}
