package org.activityinfo.server.endpoint.rest;

import com.google.inject.Provider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

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
    public AccountStatus getStatus() {

        AuthenticatedUser user = userProvider.get();
        if(user.isAnonymous()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        User userAccount = entityManager.find(User.class, user.getId());

        if(userAccount.getBillingAccount() == null) {
            return queryTrialStatus(userAccount);
        } else {
            return queryBillingStatus(userAccount);
        }
    }

    /**
     * Queries the status of a user who is not linked to a billing account
     */
    private AccountStatus queryTrialStatus(User userAccount) {

        Number databaseCount = (Number) entityManager.createNativeQuery("SELECT count(distinct d.databaseId) " +
                "FROM userdatabase d " +
                "WHERE d.ownerUserId = :userId AND d.dateDeleted IS NULL")
                .setParameter("userId", userAccount.getId())
                .getSingleResult();

        return new AccountStatus.Builder()
                .setTrial(false)
                .setUserCount(1)
                .setUserLimit(10)
                .setLegacy(userAccount.getDateCreated() == null ||
                        userAccount.getDateCreated().before(new Date(1536624000000L)))
                .setDatabaseCount(databaseCount.intValue())
                .setExpirationTime(userAccount.getTrialEndDate())
                .build();

    }

    private AccountStatus queryBillingStatus(User userAccount) {

        Number userCount = (Number) entityManager.createNativeQuery(
                "SELECT count(distinct up.userId) as userCount " +
                "FROM userlogin u " +
                "LEFT JOIN userdatabase d ON (d.OwnerUserId=u.userId) " +
                "LEFT JOIN userpermission up ON (up.databaseId=d.DatabaseId) " +
                "WHERE u.billingAccountId = :accountId and d.dateDeleted IS NULL and up.AllowView=1")
                .setParameter("accountId", userAccount.getBillingAccount().getId())
                .getSingleResult();

        return new AccountStatus.Builder()
                .setTrial(true)
                .setUserCount(userCount.intValue())
                .setUserLimit(userAccount.getBillingAccount().getUserLimit())
                .build();

    }
}
