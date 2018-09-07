package org.activityinfo.ui.client.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Active at the moment the application loads but we are waiting
 * to find out whether offline mode has been previously installed.
 */
public class PendingMode extends Mode {

    private final List<CommandRequest> pending = new ArrayList<>();

    @Override
    void init() {
    }

    @Override
    void dispatch(Command command, AsyncCallback callback) {
        pending.add(new CommandRequest(command, callback));
    }

    @Override
    OfflineStateChangeEvent.State getState() {
        return OfflineStateChangeEvent.State.CHECKING;
    }

    @Override
    public void install() {
    }

    @Override
    public void synchronize() {
    }

    @Override
    public void dispatchPendingTo(Mode mode) {
        List<CommandRequest> toDispatch = new ArrayList<>(pending);
        pending.clear();

        for (CommandRequest commandRequest : toDispatch) {
            commandRequest.dispatch(mode);
        }
    }
}
