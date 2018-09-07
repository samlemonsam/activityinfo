package org.activityinfo.ui.client.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;

class CommandRequest {
    private final Command command;
    private final AsyncCallback callback;

    public CommandRequest(Command command, AsyncCallback callback) {
        super();
        this.command = command;
        this.callback = callback;
    }

    public void dispatch(Mode dispatcher) {
        dispatcher.dispatch(command, callback);
    }
}
