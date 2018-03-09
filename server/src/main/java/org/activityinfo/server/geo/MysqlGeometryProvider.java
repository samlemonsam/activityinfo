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
package org.activityinfo.server.geo;

import com.google.common.collect.Lists;
import org.activityinfo.server.database.hibernate.entity.AdminEntity;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.List;

public class MysqlGeometryProvider implements AdminGeometryProvider {

    private final Provider<EntityManager> em;

    @Inject
    public MysqlGeometryProvider(Provider<EntityManager> em) {
        super();
        this.em = em;
    }

    @Override
    public List<AdminGeo> getGeometries(int adminLevelId) {
        List<AdminEntity> entityList = em.get()
                                         .createQuery(
                                                 "select e from AdminEntity e where e.level.id = :levelId and e" +
                                                 ".deleted = false and e.geometry is not null")
                                         .setParameter("levelId", adminLevelId)
                                         .getResultList();

        List<AdminGeo> resultList = Lists.newArrayList();
        for (AdminEntity entity : entityList) {
            resultList.add(new AdminGeo(entity.getId(), entity.getGeometry()));
        }
        return resultList;
    }

}
