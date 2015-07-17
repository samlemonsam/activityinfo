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

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.remote.RemoteDispatcher;
import org.activityinfo.legacy.client.state.GxtStateProvider;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.model.auth.AnonymousUser;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.LoggingEventBus;

public class EmbedModule extends AbstractGinModule {

    public static AuthenticatedUser getAnonymous(String localeName) {
        return new AuthenticatedUser(AnonymousUser.AUTHTOKEN,
                AuthenticatedUser.ANONYMOUS_ID,
                AnonymousUser.USER_EMAIL, localeName);
    }

    @Override
    protected void configure() {
        bind(RemoteCommandServiceAsync.class).toProvider(RemoteServiceProvider.class).in(Singleton.class);
        bind(Dispatcher.class).to(RemoteDispatcher.class).in(Singleton.class);
        bind(EventBus.class).to(LoggingEventBus.class).in(Singleton.class);
        bind(StateProvider.class).to(GxtStateProvider.class);
    }

    @Provides
    public AuthenticatedUser provideAuth() {
        return getAnonymous(LocaleInfo.getCurrentLocale().getLocaleName());
    }
}
