/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.entity.auth;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.command.handler.LegacyPermissionAdapter;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.SchemaElement;
import org.activityinfo.server.database.hibernate.entity.UserPermission;

/**
 * Checks whether the requesting user is authorized to change the given entity.
 */
public class DesignAuthorizationHandler implements AuthorizationHandler<SchemaElement> {

    @Inject
    public DesignAuthorizationHandler(LegacyPermissionAdapter legacyPermissionAdapter) {
    }

    @Override
    public boolean isAuthorized(AuthenticatedUser requestingUser, SchemaElement entity) {
        Preconditions.checkNotNull(requestingUser, "requestingUser");

        Database database = entity.findOwningDatabase();
        if (database.getOwner().getId() == requestingUser.getId()) {
            return true;
        }
        for (UserPermission permission : database.getUserPermissions()) {
            if (permission.getUser().getId() == requestingUser.getId() && permission.isAllowDesign()) {
                return true;
            }
        }
        return false;
    }
}
