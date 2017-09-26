package org.activityinfo.ui.client;

import org.activityinfo.observable.Observable;

/**
 * Provides management of the ActivityInfo AppCache.
 */
public class AppCache {

    public enum Status {
        UNCACHED,
        IDLE,
        CHECKING,
        DOWNLOADING,
        UPDATE_READY,
        OBSOLETE
    }

    private final Observable<Status> status = new ObservableStatus();

    /**
     * @return the current ActivityInfo version that is loaded and running.
     */
    public final native String getCurrentVersion() /*-{
        return $wnd.ClientContext.version;
    }-*/;

    public Observable<Status> getStatus() {
        return status;
    }

    private static class ObservableStatus extends Observable<Status> {

        public ObservableStatus() {
            sinkEvent(this, "cached");
            sinkEvent(this, "checking");
            sinkEvent(this, "downloading");
            sinkEvent(this, "noupdate");
            sinkEvent(this, "obsolete");
            sinkEvent(this, "updateready");
        }

        private native int getStatusCode() /*-{
            return $wnd.applicationCache.status;
        }-*/;

        private static native void sinkEvent(ObservableStatus observable, String eventName) /*-{
            $wnd.applicationCache.addEventListener(eventName, function(event) {
                observable.@org.activityinfo.ui.client.AppCache.ObservableStatus::fireChange()();
            })
        }-*/;

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public Status get() {
            return Status.values()[getStatusCode()];
        }

        @Override
        protected void onConnect() {
            super.onConnect();
        }
    }

}
