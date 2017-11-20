package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.EventHandler;

public interface ColumnResizeHandler extends EventHandler {

    void onColumnResized(ColumnResizeEvent columnResizeEvent);
}
