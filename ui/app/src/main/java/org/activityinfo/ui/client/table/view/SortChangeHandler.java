package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.EventHandler;

public interface SortChangeHandler extends EventHandler {

    void onSortChanged(SortChangeEvent event);

}
