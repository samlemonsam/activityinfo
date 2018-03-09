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
import org.activityinfo.server.database.hibernate.entity.AdminEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.ejb.HibernateEntityManager;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

/**
 * @author Alex Bertram
 */
public class AdminHibernateDAO extends GenericDAO<AdminEntity, Integer> implements AdminDAO {

    private final EntityManager em;

    @Inject
    public AdminHibernateDAO(EntityManager em) {
        super(em);
        this.em = em;
    }

    @Override
    public List<AdminEntity> findRootEntities(int levelId) {
        return em.createQuery(
                "select entity from AdminEntity entity where entity.level.id = :levelId order by entity.name")
                 .setParameter("levelId", levelId)
                 .getResultList();
    }

    @Override
    public List<AdminEntity> findChildEntities(int levelId, int parentEntityId) {

        return em.createQuery("select entity from AdminEntity entity where entity.level.id = :levelId " +
                              "and entity.parent.id = :parentId order by entity.name")
                 .setParameter("levelId", levelId)
                 .setParameter("parentId", parentEntityId)
                 .getResultList();
    }

    @Override
    public List<AdminEntity> find(int entityLevelId, int parentEntityId, int activityId) {
        Query q = query();
        if (activityId > -1) {
            q.withSitesOfActivityId(activityId);
        }
        if (entityLevelId > -1) {
            q.level(entityLevelId);
        }
        if (parentEntityId > -1) {
            q.withParentEntityId(parentEntityId);
        }
        return q.execute();
    }

    @Override
    public Query query() {

        final Criteria criteria = createCriteria();

        return new Query() {

            @Override
            public Query level(int levelId) {
                criteria.createAlias("entity.level", "level").add(Restrictions.eq("level.id", levelId));
                return this;
            }

            @Override
            public Query withParentEntityId(int id) {
                criteria.createAlias("entity.parent", "parent").add(Restrictions.eq("parent.id", id));
                return this;
            }

            @Override
            public Query withSitesOfActivityId(int id) {
                DetachedCriteria havingActivities = DetachedCriteria.forClass(AdminEntity.class, "entity")
                                                                    .createAlias("locations", "location")
                                                                    .createAlias("location.sites", "site")
                                                                    .createAlias("site.activity", "activity")
                                                                    .add(Restrictions.eq("activity.id", id))
                                                                    .setProjection(Projections.property("entity.id"));

                criteria.add(Subqueries.propertyIn("entity.id", havingActivities));
                return this;
            }

            @Override
            public List<AdminEntity> execute() {
                return criteria.list();
            }
        };
    }

    private Criteria createCriteria() {
        Session session = ((HibernateEntityManager) em).getSession();
        return session.createCriteria(AdminEntity.class, "entity");
    }

    @Override
    public List<AdminEntity> findBySiteIds(Set<Integer> siteIds) {
        // TODO Auto-generated method stub
        return null;
    }
}
