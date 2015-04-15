package org.activityinfo.store.query.impl;

/**
 * Slot for a value that has not yet been computed.
 *
 * <p>This class is used in place of {@code Future} for GWT compatibility and
 * because we don't need to block on the value's availability: we run all table scans
 * at once and don't touch the Slots until all queries have completed.</p>
 */
public interface Slot<T> {

    T get();
}
