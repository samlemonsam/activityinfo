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
package org.activityinfo.ui.client.offline;

import com.bedatadriven.rebar.sql.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.remote.AbstractDispatcher;
import org.activityinfo.ui.client.dispatch.remote.Remote;
import org.activityinfo.ui.client.offline.OfflineStateChangeEvent.State;

import java.util.Date;
import java.util.function.Consumer;

/**
 * OfflineController implementation for webkit-based browsers which support WebSQL.
 */
@Singleton
public class OfflineControllerWebkit extends AbstractDispatcher implements OfflineController {

    private final EventBus eventBus;
    private final Dispatcher remoteDispatcher;
    private final AuthenticatedUser user;

    private Mode mode;
    private Date lastSynced = null;

    @Inject
    public OfflineControllerWebkit(EventBus eventBus,
                                   @Remote Dispatcher remoteDispatcher,
                                   AuthenticatedUser user) {
        this.eventBus = eventBus;
        this.remoteDispatcher = remoteDispatcher;
        this.user = user;

        PendingMode pendingMode = new PendingMode();
        this.mode = pendingMode;

        isOfflineModeInstalled(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean installed) {
                if(installed) {
                    startOfflineMode(pendingMode);
                } else {
                    startOnlineMode(pendingMode);
                }
            }
        });
    }

    /**
     * Quick check to see if we need to launch offline mode at application startup.
     * @param consumer
     */
    private void isOfflineModeInstalled(Consumer<Boolean> consumer) {
        try {
            SqlDatabase database = DatabaseFactory.get(user);
            database.transaction(new SqlTransactionCallback() {
                @Override
                public void begin(SqlTransaction sqlTransaction) {
                    sqlTransaction.executeSql("select lastUpdate from sync_history", new SqlResultCallback() {
                        @Override
                        public void onSuccess(SqlTransaction sqlTransaction, SqlResultSet sqlResultSet) {
                            consumer.accept(sqlResultSet.getRows().size() > 0);
                        }
                    });
                }

                @Override
                public void onError(SqlException e) {
                    consumer.accept(false);
                }
            });
        } catch (Exception e) {
            consumer.accept(false);
        }
    }

    private void startOnlineMode(PendingMode pendingMode) {
        mode = new OnlineMode(this.remoteDispatcher);
        pendingMode.dispatchPendingTo(mode);
    }

    private void startOfflineMode(PendingMode pendingMode) {
        SwitchingMode.load(eventBus, remoteDispatcher, user, new AsyncCallback<Mode>() {
            @Override
            public void onFailure(Throwable caught) {
                startOnlineMode(pendingMode);
            }

            @Override
            public void onSuccess(Mode offlineMode) {
                mode = offlineMode;
                mode.init();
                pendingMode.dispatchPendingTo(mode);
            }
        });
    }

    @Override
    public Date getLastSyncTime() {
        return lastSynced;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public Dispatcher getDispatcher() {
        return this;
    }

    @Override
    public void install() {
        SwitchingMode.load(eventBus, remoteDispatcher, user, new AsyncCallback<Mode>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Mode offlineMode) {
                mode = offlineMode;
                mode.install();
            }
        });
    }

    @Override
    public void synchronize() {
        mode.synchronize();
    }

    @Override
    public State getState() {
        return mode.getState();
    }


    @Override
    public <T extends CommandResult> void execute(Command<T> command, AsyncCallback<T> callback) {
        mode.dispatch(command, callback);
    }

}
