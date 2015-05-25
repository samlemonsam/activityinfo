package org.activityinfo.observable;

/**
 * Created by alexander on 5/25/15.
 */
public interface TableObserver {

    /**
     * Called to notify the observer that
     */
    void onColumnsChanged(ObservableTable table);

    void onRowsChanged(ObservableTable table);

}
