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
import org.activityinfo.server.database.hibernate.EntityManagerProvider;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/postmark")
public class PostmarkWebhook {

    private static final Logger LOGGER = Logger.getLogger(PostmarkWebhook.class.getName());

    private final String postmarkToken;
    private final EntityManagerProvider entityManager;

    @Inject
    public PostmarkWebhook(DeploymentConfiguration config, EntityManagerProvider entityManager) {
        postmarkToken = config.getProperty("postmark.bouncehook.key");
        this.entityManager = entityManager;
    }

    private void checkToken(String token) {
        if (Strings.isNullOrEmpty(postmarkToken) || !postmarkToken.equals(token)) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
    }

    @POST
    @Path("/delivery")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delivery(@HeaderParam("X-Postmark-Token") String token, DeliveryReport deliveryReport) {
        checkToken(token);

        LOGGER.info("Email = " + deliveryReport.getRecipient());
        LOGGER.info("DeliveredAt = " + deliveryReport.getDeliveredAt());
        LOGGER.info("Token = " + token);

        return Response.ok().build();
    }

    @POST
    @Path("/bounce")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response bounce(@HeaderParam("X-Postmark-Token") String token, BounceReport bounceReport) {
        checkToken(token);
        removeEmailNotifications(bounceReport.getEmail());
        return Response.ok().build();
    }

    private void removeEmailNotifications(String bouncedUserEmail) {
        startTransaction();
        try {
            User bouncedUser = entityManager.get().createQuery(
                    "SELECT u FROM User " +
                            "WHERE u.email = :email", User.class)
                    .setParameter("email", bouncedUserEmail)
                    .getSingleResult();
            LOGGER.info(() -> "Removing email notifications for user " + bouncedUser.getId());
            bouncedUser.setEmailNotification(false);
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException(e);
        }
        commitTransaction();
    }

    private void commitTransaction() {
        entityManager.get().getTransaction().commit();
    }

    private void rollbackTransaction() {
        entityManager.get().getTransaction().rollback();
    }

    private void startTransaction() {
        if(!entityManager.get().getTransaction().isActive()) {
            entityManager.get().getTransaction().begin();
        }
    }

    @POST
    @Path("/spamComplaint")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response spam(@HeaderParam("X-Postmark-Token") String token, BounceReport bounceReport) {
        checkToken(token);

        LOGGER.info("Subject = " + bounceReport.getSubject());
        LOGGER.info("Email = " + bounceReport.getEmail());
        LOGGER.info("Type = " + bounceReport.getType());
        LOGGER.info("Token = " + token);

        return Response.ok().build();
    }

    @POST
    @Path("/open")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response open(@HeaderParam("X-Postmark-Token") String token, OpenReport openReport) {
        checkToken(token);

        LOGGER.info("Email = " + openReport.getRecipient());
        LOGGER.info("ReadSeconds = " + openReport.getReadSeconds());
        LOGGER.info("FirstOpen = " + openReport.isFirstOpen());
        LOGGER.info("ReceivedAt = " + openReport.getReceivedAt());
        LOGGER.info("Token = " + token);

        return Response.ok().build();
    }

    @POST
    @Path("/linkClick")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response link(@HeaderParam("X-Postmark-Token") String token, ClickReport clickReport) {
        checkToken(token);

        LOGGER.info("Email = " + clickReport.getRecipient());
        LOGGER.info("Link = " + clickReport.getOriginalLink());
        LOGGER.info("ReceivedAt = " + clickReport.getReceivedAt());
        LOGGER.info("Token = " + token);

        return Response.ok().build();
    }
}
