package org.activityinfo.store.query.impl.builders;

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.impl.Slot;

/**
 * Builds a "vertical" view of a single FormField value, or multiple combinations
 * of values.
 */
public interface ColumnViewBuilder extends Slot<ColumnView> {

    void setFromCache(ColumnView view);
}
