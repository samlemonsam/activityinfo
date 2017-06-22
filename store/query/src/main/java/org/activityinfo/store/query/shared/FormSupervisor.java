package org.activityinfo.store.query.shared;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormPermissions;

/**
 * Provides form- and record-level permissions to the query engine.
 */
public interface FormSupervisor {

    FormPermissions getFormPermissions(ResourceId formId);

}
