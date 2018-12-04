package org.activityinfo.server.endpoint.rest;

import com.google.inject.Provider;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.account.AccountStatus;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/resources/account")
public class AccountResource {

    private final EntityManager entityManager;
    private final Provider<AuthenticatedUser> userProvider;

    public AccountResource(EntityManager entityManager, Provider<AuthenticatedUser> userProvider) {
        this.entityManager = entityManager;
        this.userProvider = userProvider;
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountStatus getStatus(@InjectParam BillingAccountOracle accountOracle) {

        AuthenticatedUser user = userProvider.get();
        if(user.isAnonymous()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return accountOracle.getStatus(user);
    }
}
