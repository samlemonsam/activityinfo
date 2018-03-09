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

import javax.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;

/**
 * Generic DAO implementation which provides implementation of boiler-plate
 * methods using generics.
 *
 * @param <T> Entity Type
 * @param <K> Entity Key Type
 * @author Alex Bertram
 */
public abstract class GenericDAO<T, K> implements DAO<T, K> {
    private final Class<T> persistentClass;
    private final EntityManager em;

    protected GenericDAO(EntityManager em) {
        this.em = em;
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected final EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void persist(T entity) {
        this.em.persist(entity);
    }

    @Override
    public T findById(K primaryKey) {
        return this.em.find(persistentClass, primaryKey);
    }
}
