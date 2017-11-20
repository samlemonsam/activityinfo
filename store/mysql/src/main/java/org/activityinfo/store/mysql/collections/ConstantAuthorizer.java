package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormPermissions;

/**
 * Authorizer which grants the same permissions for all users
 */
public class ConstantAuthorizer implements Authorizer {
    private final FormPermissions permissions;

    public ConstantAuthorizer(FormPermissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return permissions;
    }
    
    public static ConstantAuthorizer full() {
        return new ConstantAuthorizer(FormPermissions.full());
    }
    
    public static ConstantAuthorizer readonly() {
        return new ConstantAuthorizer(FormPermissions.readonly());
    }
    
}
