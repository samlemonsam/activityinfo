package org.activityinfo.ui.client.offline;

import com.bedatadriven.rebar.sql.client.SqlDatabase;
import com.google.common.base.Function;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.inject.ClientSideAuthProvider;
import org.activityinfo.ui.client.offline.sync.*;
import org.activityinfo.ui.client.offline.sync.pipeline.InstallPipeline;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SwitchingMode extends Mode {

    private final EventBus eventBus;
    private final Dispatcher remoteDispatcher;
    private final SqlDatabase database;

    private final List<CommandRequest> pending = new ArrayList<>();
    private final AuthenticatedUser user;

    private Date lastSynced;

    private Mode delegate = new PendingMode();
    private OfflineStateChangeEvent.State state;

    public static void load(EventBus eventBus, Dispatcher remoteDispatcher, AuthenticatedUser user, AsyncCallback<Mode> callback) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess() {
                callback.onSuccess(new SwitchingMode(eventBus, remoteDispatcher, user));
            }
        });
    }

    public SwitchingMode(EventBus eventBus, Dispatcher remoteDispatcher, AuthenticatedUser user) {
        this.eventBus = eventBus;
        this.remoteDispatcher = remoteDispatcher;
        this.database = DatabaseFactory.get(user);
        this.user = user;
        changeState(OfflineStateChangeEvent.State.CHECKING);
    }


    @Override
    void init() {
        // ensure that's the user's authentication is persisted across sessions!
        ClientSideAuthProvider.persistAuthentication();

        // Query the last time
        queryLastSyncTime().join(new Function<Date, Promise<Void>>() {
            @Override
            public Promise<Void> apply(@Nullable Date date) {
                lastSynced = date;
                eventBus.fireEvent(new SyncCompleteEvent(lastSynced));
                return Promise.done();
            }
        }).join(new Function<Void, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Void nothing) {
                return migrateSchema();
            }
        }).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                fallbackToOnlineMode();
            }

            @Override
            public void onSuccess(Void result) {
                transitionToOfflineMode();
            }
        });
    }


    @Override
    public void install() {
        changeState(OfflineStateChangeEvent.State.INSTALLING);
        InstallPipeline pipeline = new InstallPipeline(database, eventBus, remoteDispatcher);
        pipeline.start(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                reportFailure(caught);
                fallbackToOnlineMode();
            }

            @Override
            public void onSuccess(Void result) {
                transitionToOfflineMode();
            }
        });

    }

    private void changeState(OfflineStateChangeEvent.State newState) {
        state = newState;
        eventBus.fireEvent(new OfflineStateChangeEvent(newState));
    }

    private Promise<Date> queryLastSyncTime() {
        Promise<Date> promise = new Promise<>();
        SyncHistoryTable historyTable = new SyncHistoryTable(database);
        historyTable.get(promise);
        return promise;
    }

    private Promise<Void> migrateSchema() {
        SchemaMigration migration = new SchemaMigration(database);
        return migration.migrate();
    }

    @Override
    public void synchronize() {
        delegate.synchronize();
    }

    @Override
    void dispatch(Command command, AsyncCallback callback) {
        delegate.dispatch(command, callback);
    }

    @Override
    OfflineStateChangeEvent.State getState() {
        return state;
    }


    private void fallbackToOnlineMode() {
        OnlineMode onlineMode = new OnlineMode(remoteDispatcher);
        delegate.dispatchPendingTo(onlineMode);
        this.delegate = onlineMode;
        changeState(OfflineStateChangeEvent.State.UNINSTALLED);
    }


    private void transitionToOfflineMode() {
        OfflineMode offlineMode = new OfflineMode(eventBus, user, database, remoteDispatcher);
        delegate.dispatchPendingTo(offlineMode);
        this.delegate = offlineMode;
        changeState(OfflineStateChangeEvent.State.INSTALLED);

        // Trigger an initial synchronization
        this.delegate.synchronize();
    }

    private void reportFailure(Throwable throwable) {
        Log.error("Exception in offline controller", throwable);

        eventBus.fireEvent(new SyncErrorEvent(SyncErrorType.fromException(throwable)));
    }
}
