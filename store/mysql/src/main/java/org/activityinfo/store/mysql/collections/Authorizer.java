package org.activityinfo.store.mysql.collections;

import org.activityinfo.service.store.CollectionPermissions;

import java.util.Collections;

/**
 * Provides permissions on a collection for a specific User
 */
public interface Authorizer {
    
    CollectionPermissions getPermissions(int userId);
}
