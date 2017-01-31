package org.activityinfo.ui.client.component.formdesigner.drop;

import com.google.gwt.cell.client.ValueUpdater;

public enum NullValueUpdater implements ValueUpdater {
    INSTANCE;

    @Override
    public void update(Object value) {
        // No action
    }
}
