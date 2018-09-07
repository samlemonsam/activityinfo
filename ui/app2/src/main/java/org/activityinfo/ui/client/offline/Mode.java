package org.activityinfo.ui.client.offline;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;

public abstract class Mode {
    Mode activate() {
        return this;
    }

    abstract void init();

    abstract void dispatch(Command command, AsyncCallback callback);

    abstract OfflineStateChangeEvent.State getState();

    public abstract void install();

    public abstract void synchronize();

    public void dispatchPendingTo(Mode mode) {
    }
}
