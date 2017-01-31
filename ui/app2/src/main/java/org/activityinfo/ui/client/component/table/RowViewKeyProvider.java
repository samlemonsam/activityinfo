package org.activityinfo.ui.client.component.table;

import com.google.gwt.view.client.ProvidesKey;

/**
 * Provides keys for Projections
 */
public class RowViewKeyProvider implements ProvidesKey<RowView> {
    @Override
    public String getKey(RowView row) {
        return row.getId();
    }
}
