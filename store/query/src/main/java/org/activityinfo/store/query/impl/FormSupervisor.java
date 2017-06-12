package org.activityinfo.store.query.impl;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormPermissions;

/**
 * Applies record-level permissions
 */
public interface FormSupervisor {

    FormPermissions getFormPermissions(ResourceId formId);

}
