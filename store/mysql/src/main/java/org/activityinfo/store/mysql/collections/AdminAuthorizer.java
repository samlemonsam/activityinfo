package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormPermissions;

/**
 * Hard-coded permissions for global ActivityInfo administrator (id=3)
 *
 * This needed until we migrate the administrative levels to "normal" forms.
 */
public class AdminAuthorizer implements Authorizer {
    @Override
    public FormPermissions getPermissions(int userId) {
        if(userId == 3) {
            return FormPermissions.full();
        } else {
            return FormPermissions.readonly();
        }
    }
}
