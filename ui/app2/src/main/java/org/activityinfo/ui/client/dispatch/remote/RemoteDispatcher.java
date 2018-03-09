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
package org.activityinfo.ui.client.dispatch.remote;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;

import java.util.Collections;
import java.util.List;

/**
 * Dispatcher which sends individual commands to the server with no caching,
 * batching, or retrying.
 */
public class RemoteDispatcher extends AbstractDispatcher {
    private final AuthenticatedUser auth;
    private final RemoteCommandServiceAsync service;
    private final String locale;

    @Inject
    public RemoteDispatcher(AuthenticatedUser auth, RemoteCommandServiceAsync service) {
        this.auth = auth;
        this.service = service;
        this.locale = LocaleInfo.getCurrentLocale().getLocaleName();
    }

    public RemoteDispatcher(AuthenticatedUser auth, RemoteCommandServiceAsync service, String locale) {
        this.auth = auth;
        this.service = service;
        this.locale = locale;
    }

    @Override
    public <T extends CommandResult> void execute(final Command<T> command, final AsyncCallback<T> callback) {
        try {
            System.currentTimeMillis();
            service.execute(auth.getAuthToken(),
                    locale,
                    Collections.singletonList((Command) command),
                    new AsyncCallback<List<CommandResult>>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            callback.onFailure(throwable);
                        }

                        @Override
                        public void onSuccess(List<CommandResult> commandResults) {
                            CommandResult result = commandResults.get(0);
                            if (result instanceof CommandException) {
                                callback.onFailure((CommandException) result);
                            } else {
                                callback.onSuccess((T) result);
                            }
                        }
                    });
        } catch (Exception e) {
            // catch client-side serialization exceptions
            callback.onFailure(e);
        }
    }
}
