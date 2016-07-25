package org.activityinfo.store.mysql.collections;

import org.activityinfo.service.store.FormPermissions;

/**
 * Provides permissions on a collection for a specific User
 */
public interface Authorizer {
    
    FormPermissions getPermissions(int userId);
}
