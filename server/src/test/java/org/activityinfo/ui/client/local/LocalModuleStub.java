package org.activityinfo.ui.client.local;

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

import com.bedatadriven.rebar.sql.client.SqlDatabase;
import com.bedatadriven.rebar.sql.client.query.SqlDialect;
import com.bedatadriven.rebar.sql.client.query.SqliteDialect;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.remote.Remote;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.MockEventBus;
import org.activityinfo.ui.client.local.command.HandlerRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LocalModuleStub extends AbstractModule {

    private final SqlDatabase database;
    private final AuthenticatedUser authentication;
    private Dispatcher remoteDispatcher;

    public LocalModuleStub(AuthenticatedUser auth, SqlDatabase database,
                           Dispatcher remoteDispatcher) {
        this.database = database;
        this.authentication = auth;
        this.remoteDispatcher = remoteDispatcher;
    }

    @Override
    protected void configure() {
        bind(SqlDatabase.class).toInstance(database);
        bind(AuthenticatedUser.class).toInstance(authentication);
        bind(HandlerRegistry.class).toProvider(HandlerRegistryProvider.class);
        bind(SqlDialect.class).to(SqliteDialect.class);
        bind(Dispatcher.class).annotatedWith(Remote.class).toInstance(
                remoteDispatcher);
        bind(EventBus.class).to(MockEventBus.class);
    }

    @Provides
    public UiConstants provideConstants() {
        return (UiConstants) Proxy.newProxyInstance(this.getClass()
                .getClassLoader(), new Class[]{UiConstants.class},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object instance, Method method,
                                         Object[] arguments)
                            throws Throwable {
                        return method.getName();
                    }
                });
    }
}
