package org.activityinfo.ui.client.offline;

import com.bedatadriven.rebar.sql.client.SqlDatabase;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.offline.command.CommandQueue;
import org.activityinfo.ui.client.offline.command.OfflineDispatcher;
import org.activityinfo.ui.client.offline.sync.DownSynchronizer;
import org.activityinfo.ui.client.offline.sync.SyncRequestEvent;
import org.activityinfo.ui.client.offline.sync.SynchronizerDispatcher;
import org.activityinfo.ui.client.offline.sync.UpdateSynchronizer;
import org.activityinfo.ui.client.offline.sync.pipeline.SyncPipeline;

public class OfflineMode extends Mode {

    private static final int AUTO_SYNC_INTERVAL_MS = 5 * 60 * 1000;

    private final EventBus eventBus;
    private final OfflineDispatcher offlineDispatcher;
    private final SyncPipeline syncPipeline;

    public OfflineMode(EventBus eventBus, AuthenticatedUser user, SqlDatabase database, Dispatcher remoteDispatcher) {
        this.eventBus = eventBus;
        CommandQueue commandQueue = new CommandQueue(eventBus, database);

        offlineDispatcher = new OfflineDispatcher(eventBus, database, user, remoteDispatcher, commandQueue);
        syncPipeline = new SyncPipeline(
                new UpdateSynchronizer(commandQueue, new SynchronizerDispatcher(eventBus, remoteDispatcher)),
                new DownSynchronizer(eventBus, remoteDispatcher, database));

        eventBus.addListener(SyncRequestEvent.TYPE, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent baseEvent) {
                synchronize();
            }
        });
    }

    @Override
    void init() {
    }

    @Override
    void dispatch(Command command, AsyncCallback callback) {
        offlineDispatcher.execute(command, callback);
    }

    @Override
    OfflineStateChangeEvent.State getState() {
        return OfflineStateChangeEvent.State.INSTALLED;
    }

    @Override
    public void install() {
    }

    @Override
    public void synchronize() {
        syncPipeline.start(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                scheduleNext();
            }

            @Override
            public void onSuccess(Void result) {
                scheduleNext();
            }
        });

    }

    private void scheduleNext() {
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                synchronize();
                return false;
            }
        }, AUTO_SYNC_INTERVAL_MS);
    }

}
