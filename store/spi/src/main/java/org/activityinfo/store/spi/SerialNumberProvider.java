package org.activityinfo.store.spi;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;

/**
 * Provides a sequential file number at the form level
 */
public interface SerialNumberProvider {

    int next(ResourceId formId, ResourceId fieldId);
}
