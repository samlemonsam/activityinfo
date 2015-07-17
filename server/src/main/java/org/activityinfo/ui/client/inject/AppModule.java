package org.activityinfo.ui.client.inject;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.client.DispatchEventSource;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.remote.MergingDispatcher;
import org.activityinfo.legacy.client.remote.Remote;
import org.activityinfo.legacy.client.remote.RemoteDispatcher;
import org.activityinfo.legacy.client.remote.cache.CacheManager;
import org.activityinfo.legacy.client.remote.cache.CachingDispatcher;
import org.activityinfo.legacy.client.state.GxtStateProvider;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.LoggingEventBus;
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
        return new ResourceLocatorAdaptor(dispatcher);
    }
}
