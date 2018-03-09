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
package org.activityinfo.server.branding;

import com.google.common.collect.Maps;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.database.hibernate.entity.Domain;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/admin/branding")
public class BrandingConfigResource {

    @GET @Produces(MediaType.TEXT_HTML) @Path("{host}")
    public Viewable getPage(@InjectParam EntityManager em, @PathParam("host") String host) {

        Domain domain = em.find(Domain.class, host);
        if (domain == null) {
            domain = new Domain();
            domain.setHost(host);
        }

        Map<String, Object> model = Maps.newHashMap();
        model.put("customDomain", domain);

        return new Viewable("/page/BrandingConfig.ftl", model);
    }

    @POST @Path("{host}")
    public Response updateConfig(@InjectParam EntityManager em,
                                 @Context UriInfo uri,
                                 @PathParam("host") String host,
                                 @FormParam("title") String updatedTitle,
                                 @FormParam("scaffolding") String updatedScaffolding,
                                 @FormParam("homePageBody") String updatedHomePageBody) {

        em.getTransaction().begin();

        Domain domain = em.find(Domain.class, host);
        if (domain == null) {
            domain = new Domain();
            domain.setHost(host);
        }

        domain.setTitle(updatedTitle);
        domain.setScaffolding(updatedScaffolding);
        domain.setHomePageBody(updatedHomePageBody);

        em.persist(domain);
        em.getTransaction().commit();

        return Response.seeOther(uri.getRequestUri()).build();
    }

}
