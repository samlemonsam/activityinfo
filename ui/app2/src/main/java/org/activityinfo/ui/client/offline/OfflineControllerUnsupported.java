package org.activityinfo.ui.client.offline;

import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.Date;

public class OfflineControllerUnsupported implements OfflineController {

    private final Dispatcher remoteDispatcher;

    public OfflineControllerUnsupported(Dispatcher remoteDispatcher) {
        this.remoteDispatcher = remoteDispatcher;
    }

    @Override
    public OfflineStateChangeEvent.State getState() {
        return OfflineStateChangeEvent.State.UNINSTALLED;
    }

    @Override
    public void install() {
        UnsupportedDialog.show();
    }

    @Override
    public void synchronize() {
    }

    @Override
    public Date getLastSyncTime() {
        return null;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public Dispatcher getDispatcher() {
        return remoteDispatcher;
    }
}
