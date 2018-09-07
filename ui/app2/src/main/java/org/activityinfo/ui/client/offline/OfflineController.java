package org.activityinfo.ui.client.offline;

import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.Date;

/**
 * Provides an interface to query the status of offline synchronization and control synchronization.
 *
 * <p>The implementation of this interface is chosen at compile time depending on whether or not the
 * browser we are targeting supports WebSQL. (Only Chrome, actually)</p>
 */
public interface OfflineController {

    /**
     * @return the current state of offline mode
     */
    OfflineStateChangeEvent.State getState();

    /**
     * Starts the process of "installing" offline mode, include the initial download.
     */
    void install();

    /**
     * Starts a synchronization attempt
     */
    void synchronize();

    /**
     * @return the date and time of the last successfull synchronization, or {@code null}
     * if there has never been a successful synchronization.
     */
    Date getLastSyncTime();

    /**
     * @return true if this browser supports offline mode
     */
    boolean isSupported();

    /**
     * @return the dispatcher to use for executing remote commands.
     */
    Dispatcher getDispatcher();
}
