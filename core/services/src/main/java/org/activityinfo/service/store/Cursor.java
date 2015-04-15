package org.activityinfo.service.store;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

/**
 * Contract for a cursor capable of iterating over a collection
 * of Resources.
 *
 * <p>The {@code Cursor} is initially positioned before the start of the
 * first {@code Resource}, call {@code next()} to advance to the first
 * Resource.
 */
public interface Cursor {

    /**
     * Advances the cursor to the next {@code Resource}
     *
     * @return false if the cursor is currently positioned on the last
     * resource.
     */
    boolean next();

    /**
     *
     * @return the {@code ResourceId} of the Resource at the cursor's current position.
     */
    ResourceId getResourceId();

}
