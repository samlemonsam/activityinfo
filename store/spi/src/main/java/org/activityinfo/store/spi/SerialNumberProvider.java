package org.activityinfo.store.spi;

import org.activityinfo.model.resource.ResourceId;

/**
 * Provides a sequential file number at the form level
 */
public interface SerialNumberProvider {

    /**
     * Generates the next serial number in a sequence for the given form, field,
     * and prefix.
     */
    int next(ResourceId formId, ResourceId fieldId, String prefix);
}
