package org.activityinfo.ui.client.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public class OnlineMode extends Mode {
    private final Dispatcher dispatcher;

    public OnlineMode(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    void init() {
        // NOOP
    }

    @Override
    void dispatch(Command command, AsyncCallback callback) {
        dispatcher.execute(command, callback);
    }

    @Override
    OfflineStateChangeEvent.State getState() {
        return OfflineStateChangeEvent.State.UNINSTALLED;
    }

    @Override
    public void install() {
        // NOOP
    }

    @Override
    public void synchronize() {
        // NOOP
    }
}
