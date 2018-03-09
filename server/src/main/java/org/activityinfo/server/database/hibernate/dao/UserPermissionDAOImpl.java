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
package org.activityinfo.server.database.hibernate.dao;

import com.google.inject.Inject;
import org.activityinfo.server.database.hibernate.entity.UserPermission;

import javax.persistence.EntityManager;
import java.util.List;

public class UserPermissionDAOImpl extends GenericDAO<UserPermission, Integer> implements UserPermissionDAO {

    @Inject
    public UserPermissionDAOImpl(EntityManager em) {
        super(em);
    }

    @Override @SuppressWarnings("unchecked")
    public UserPermission findUserPermissionByUserIdAndDatabaseId(int userId, int databaseId) {
        List<UserPermission> result = getEntityManager().createNamedQuery("findUserPermissionByUserIdAndDatabaseId")
                                                        .setParameter("databaseId", databaseId)
                                                        .setParameter("userId", userId)
                                                        .getResultList();
        return (result != null && !result.isEmpty()) ? result.get(0) : null;
    }
}