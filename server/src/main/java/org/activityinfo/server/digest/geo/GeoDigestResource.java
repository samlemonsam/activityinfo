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
package org.activityinfo.server.digest.geo;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.server.digest.DigestResource;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Path;
import java.util.List;

@Path(GeoDigestResource.ENDPOINT)
public class GeoDigestResource extends DigestResource {
    public static final String ENDPOINT = "/tasks/geodigests";

    private final Provider<EntityManager> entityManager;

    @Inject
    public GeoDigestResource(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public String getUserDigestEndpoint() {
        return GeoUserDigestResource.ENDPOINT;
    }

    /**
     * @return the ids of all users who could possibly be selected to recieve the digest email. Filter on database
     * ownership and userpermission.allowView to minimize the amount of created userdigest tasks.
     */
    @Override @SuppressWarnings("unchecked")
    public List<Integer> selectUsers() {
        // @formatter:off
        Query query = entityManager.get().createNativeQuery("select u.userid from userlogin u " +
                                                            "where u.emailnotification and ( " +
                                                            "exists ( " +
                                                            "select 1 from userdatabase d " +
                                                            "where d.owneruserid = u.userid " +
                                                            ") " +
                                                            "or " +
                                                            "exists ( " +
                                                            "select 1 from userpermission p " +
                                                            "where p.userId = u.userid " +
                                                            "and p.allowView " +
                                                            ") " +
                                                            ")");
        // @formatter:on
        return query.getResultList();
    }
}
