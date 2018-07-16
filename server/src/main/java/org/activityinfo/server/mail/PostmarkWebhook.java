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
import com.google.inject.Provider;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/postmark")
public class PostmarkWebhook {

    private static final Logger LOGGER = Logger.getLogger(PostmarkWebhook.class.getName());

    private static final String TRACK_BOUNCE = "postmark_bounce";
    private static final String TRACK_OPEN = "postmark_open";
    private static final String TRACK_DELIVERY = "postmark_delivery";
    private static final String TRACK_SPAM_COMPLAINT = "postmark_spam";
    private static final String TRACK_LINK_CLICK = "postmark_linkClick";

    private final String postmarkToken;
    private final Provider<EntityManager> entityManagerProvider;

    @Inject
    public PostmarkWebhook(DeploymentConfiguration config, Provider<EntityManager> entityManagerProvider) {
        postmarkToken = config.getProperty("postmark.bouncehook.key");
        this.entityManagerProvider = entityManagerProvider;
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

        LOGGER.info("MessageId = " + deliveryReport.getMessageId());
        LOGGER.info("DeliveredAt = " + deliveryReport.getDeliveredAt());

        User recipientUser = getUser(deliveryReport.getRecipient());
        if (recipientUser == null) {
            LOGGER.warning("User not found for recipient.");
            return Response.ok().build();
        }

        LOGGER.info(() -> "TRACK User " + recipientUser.getId() + " " + TRACK_DELIVERY);

        return Response.ok().build();
    }

    @POST
    @Path("/bounce")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response bounce(@HeaderParam("X-Postmark-Token") String token, BounceReport bounceReport) {
        checkToken(token);

        LOGGER.info("MessageId = " + bounceReport.getMessageId());

        User bouncedUser = getUser(bounceReport.getEmail());
        if (bouncedUser == null) {
            LOGGER.warning("User not found for bounced email.");
            return Response.ok().build();
        }

        LOGGER.info(() -> "TRACK User " + bouncedUser.getId() + " " + TRACK_BOUNCE);
        removeEmailNotifications(bouncedUser);

        return Response.ok().build();
    }

    private void removeEmailNotifications(User bouncedUser) {
        startTransaction();
        try {
            LOGGER.info(() -> "Removing email notifications for user " + bouncedUser.getId());
            bouncedUser.setEmailNotification(false);
            entityManagerProvider.get().persist(bouncedUser);
        } catch (Exception e) {
            rollbackTransaction();
            throw new RuntimeException(e);
        }
        commitTransaction();
    }

    private User getUser(String userEmail) {
        try {
            return entityManagerProvider.get().createQuery(
                    "SELECT u FROM User u " +
                            "WHERE u.email = :email", User.class)
                    .setParameter("email", userEmail)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void commitTransaction() {
        entityManagerProvider.get().getTransaction().commit();
    }

    private void rollbackTransaction() {
        entityManagerProvider.get().getTransaction().rollback();
    }

    private void startTransaction() {
        if(!entityManagerProvider.get().getTransaction().isActive()) {
            entityManagerProvider.get().getTransaction().begin();
        }
    }

    @POST
    @Path("/spamComplaint")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response spam(@HeaderParam("X-Postmark-Token") String token, BounceReport bounceReport) {
        checkToken(token);

        LOGGER.info("MessageId = " + bounceReport.getMessageId());
        LOGGER.info("Subject = " + bounceReport.getSubject());

        User complainant = getUser(bounceReport.getEmail());
        if (complainant == null) {
            LOGGER.warning("User filing spam report not found.");
            return Response.ok().build();
        }

        LOGGER.info(() -> "TRACK User " + complainant.getId() + " " + TRACK_SPAM_COMPLAINT);

        return Response.ok().build();
    }

    @POST
    @Path("/open")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response open(@HeaderParam("X-Postmark-Token") String token, OpenReport openReport) {
        checkToken(token);

        LOGGER.info("MessageId = " + openReport.getMessageId());
        LOGGER.info("ReadSeconds = " + openReport.getReadSeconds());
        LOGGER.info("FirstOpen = " + openReport.isFirstOpen());
        LOGGER.info("ReceivedAt = " + openReport.getReceivedAt());

        User readingUser = getUser(openReport.getRecipient());
        if (readingUser == null) {
            LOGGER.warning("User not found for given email.");
            return Response.ok().build();
        }

        LOGGER.info(() -> "TRACK User " + readingUser.getId() + " " + TRACK_OPEN);

        return Response.ok().build();
    }

    @POST
    @Path("/linkClick")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response link(@HeaderParam("X-Postmark-Token") String token, ClickReport clickReport) {
        checkToken(token);

        LOGGER.info("MessageId = " + clickReport.getMessageId());
        LOGGER.info("Link = " + clickReport.getOriginalLink());
        LOGGER.info("ReceivedAt = " + clickReport.getReceivedAt());

        User clickThruUser = getUser(clickReport.getRecipient());
        if (clickThruUser == null) {
            LOGGER.warning("User not found for given email.");
            return Response.ok().build();
        }

        LOGGER.info(() -> "TRACK User " + clickThruUser.getId() + " " + TRACK_LINK_CLICK);

        return Response.ok().build();
    }
}
