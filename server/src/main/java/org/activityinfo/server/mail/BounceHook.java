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
package org.activityinfo.server.mail;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.activityinfo.server.DeploymentConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/postmark")
public class BounceHook {

    private static final Logger LOGGER = Logger.getLogger(BounceHook.class.getName());

    private final String postmarkToken;

    @Inject
    public BounceHook(DeploymentConfiguration config) {
        postmarkToken = config.getProperty("postmark.bouncehook.key");
    }

    @POST 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mailStatus(@HeaderParam("X-Postmark-Token") String token, MailReport mailReport) {

        if (Strings.isNullOrEmpty(postmarkToken) || !postmarkToken.equals(token)) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        LOGGER.info("Subject = " + mailReport.getSubject());
        LOGGER.info("Email = " + mailReport.getEmail());
        LOGGER.info("Type = " + mailReport.getType());
        LOGGER.info("Token = " + token);

        return Response.ok().build();
    }
}
