package org.activityinfo.store.mysql.collections;

import org.activityinfo.service.store.CollectionPermissions;

/**
 * Authorizer which grants the same permissions for all users
 */
public class ConstantAuthorizer implements Authorizer {
    private final CollectionPermissions permissions;

    public ConstantAuthorizer(CollectionPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        return permissions;
    }
    
    public static ConstantAuthorizer full() {
        return new ConstantAuthorizer(CollectionPermissions.full());
    }
    
    public static ConstantAuthorizer readonly() {
        return new ConstantAuthorizer(CollectionPermissions.readonly());
    }
    
}
