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
package org.activityinfo.server.login;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.authentication.AuthTokenProvider;
import org.activityinfo.server.database.hibernate.dao.PartnerDAO;
import org.activityinfo.server.database.hibernate.dao.UserDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.dao.UserPermissionDAO;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.server.login.model.SignUpConfirmationInvalidPageModel;
import org.activityinfo.server.login.model.SignUpConfirmationPageModel;
import org.activityinfo.server.util.MailingListClient;
import org.activityinfo.store.query.UsageTracker;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path(SignUpConfirmationController.ENDPOINT)
public class SignUpConfirmationController {
    public static final String ENDPOINT = "/signUpConfirmation";

    private static final Logger LOGGER = Logger.getLogger(SignUpConfirmationController.class.getName());

    private static final int MAX_PARAM_LENGTH = 200;

    private static final int DEFAULT_DATABASE_ID = 507; // training DB
    private static final int DEFAULT_PARTNER_ID = 563; // brc

    private final MailingListClient mailingList;

    private final Provider<UserDAO> userDAO;
    private final EntityManager entityManager;
    private final AuthTokenProvider authTokenProvider;
    private final BillingAccountOracle billingAccountOracle;

    @Inject
    public SignUpConfirmationController(Provider<UserDAO> userDAO,
                                        Provider<UserDatabaseDAO> databaseDAO,
                                        EntityManager entityManager, Provider<PartnerDAO> partnerDAO,
                                        Provider<UserPermissionDAO> permissionDAO,
                                        MailingListClient mailChimp,
                                        AuthTokenProvider authTokenProvider,
                                        BillingAccountOracle billingAccountOracle) {
        super();
        this.userDAO = userDAO;
        this.entityManager = entityManager;
        this.authTokenProvider = authTokenProvider;
        this.mailingList = mailChimp;
        this.billingAccountOracle = billingAccountOracle;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPage(@Context UriInfo uri) throws Exception {
        try {
            User user = userDAO.get().findUserByChangePasswordKey(uri.getRequestUri().getQuery());

            UsageTracker.track(user.getId(), "signup_confirm_start");

            return new SignUpConfirmationPageModel(user.getChangePasswordKey()).asViewable();
        } catch (NoResultException e) {
            return new SignUpConfirmationInvalidPageModel().asViewable();
        }
    }

    @POST
    public Response confirm(@Context UriInfo uri,
                            @FormParam("key") String key,
                            @FormParam("password") String password,
                            @FormParam("newsletter") boolean newsletter) {

        entityManager.getTransaction().begin();

        try {
            // check params
            checkParam(key, true);
            checkParam(password, true);

            // confirm user
            User user = userDAO.get().findUserByChangePasswordKey(key);
            user.changePassword(password);
            user.clearChangePasswordKey();
            user.setEmailNotification(true);

            billingAccountOracle.startFreeTrial(user);

            UsageTracker.track(user.getId(), "signup_confirm_finish");
            UsageTracker.track(user.getId(), "login");

            // add user to default database
            addUserToDefaultDatabase(user);

            entityManager.getTransaction().commit();

            mailingList.subscribe(user, false, newsletter);


            // go to the home page
            return Response.seeOther(uri.getAbsolutePathBuilder().replacePath("/app").build())
                    .cookie(authTokenProvider.createNewAuthCookies(user))
                    .build();

        } catch (Exception e) {

            
            LOGGER.log(Level.SEVERE, "Exception during signup process", e);
            return Response.ok(SignUpConfirmationPageModel.genericErrorModel(key).asViewable())
                .type(MediaType.TEXT_HTML)
                .build();
        }
    }
    protected void addUserToDefaultDatabase(User user) {
        Database database = entityManager.find(Database.class, DEFAULT_DATABASE_ID);
        if(database == null) {
            LOGGER.severe("Default database " + DEFAULT_DATABASE_ID + " does not exist, unable to add user " + user.getEmail());
            return;
        }

        Partner partner = entityManager.find(Partner.class, DEFAULT_PARTNER_ID);
        if(partner == null) {
            LOGGER.severe("Default partner " + DEFAULT_PARTNER_ID + " does not exist, unable to add user " + user.getEmail());
            return;
        }
        UserPermission permission = new UserPermission(database, user);
        permission.addPartner(partner);
        permission.setAllowView(true);
        permission.setAllowViewAll(true);
        permission.setLastSchemaUpdate(new Date());
        entityManager.persist(permission);
    }

    private void checkParam(String value, boolean required) {
        boolean illegal = false;
        illegal = (required && Strings.isNullOrEmpty(value));
        illegal |= (value != null && value.length() > MAX_PARAM_LENGTH); // sanity check

        if (illegal) {
            throw new IllegalArgumentException();
        }
    }
}
