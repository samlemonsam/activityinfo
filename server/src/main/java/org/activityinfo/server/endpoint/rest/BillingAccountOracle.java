package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Singleton
public class BillingAccountOracle {

    private final Provider<EntityManager> entityManager;


    @Inject
    public BillingAccountOracle(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    public AccountStatus getStatus(AuthenticatedUser user) {
        User userAccount = entityManager.get().find(User.class, user.getId());
        return getStatus(userAccount);
    }

    public AccountStatus getStatus(User userAccount) {
        if(userAccount.getBillingAccount() != null) {
            return queryBillingStatus(userAccount);
        }

        if(userAccount.getTrialEndDate() != null) {
            return queryTrialStatus(userAccount);
        }

        // Otherwise, no trial started, and no billing account
        return new AccountStatus.Builder()
                .setUserAccountId(userAccount.getId())
                .setLegacy(false)
                .setTrial(false)
                .setDatabaseCount(0)
                .setUserLimit(0)
                .setUserCount(0)
                .setExpirationTime(new LocalDate(2999,1,1))
                .build();
    }

    public AccountStatus getStatusOrStartFreeTrial(User user) {
        if(user.getBillingAccount() == null && user.getTrialEndDate() == null) {
            startFreeTrial(user);
        }
        return getStatus(user);
    }


    public AccountStatus getStatusForDatabase(int databaseId) {
        Database database = entityManager.get().find(Database.class, databaseId);
        return getStatus(database.getOwner());
    }

    /**
     * Determine whether this database's billing account has room to add the additional user
     *
     * @param newUser
     * @param database
     * @return
     */
    public boolean isAllowAddUser(User newUser, Database database) {

        AccountStatus accountStatus = getStatus(database.getOwner());
        if(accountStatus.getUserCount() < accountStatus.getUserLimit()) {
            return true;
        }

        // Otherwise we need to check to see if this user is already present in another database
        if(accountStatus.isTrial()) {
            return queryUserExistsInTrialAccount(database.getOwner().getId(), newUser.getId());
        } else {
            return queryUserExistsInBillingAccount(database.getOwner().getBillingAccount().getId(), newUser.getId());
        }
    }

    /**
     * Queries the status of a user who is not linked to a billing account
     */
    private AccountStatus queryTrialStatus(User userAccount) {


        Object[] result = (Object[]) entityManager.get().createNativeQuery(
            "SELECT count(distinct d.databaseId), count(distinct up.userId) " +
                "FROM userdatabase d " +
                "LEFT JOIN userpermission up ON (up.databaseId=d.databaseId AND up.AllowView=1) " +
                "WHERE d.ownerUserId = :userId AND d.dateDeleted IS NULL")
                .setParameter("userId", userAccount.getId())
                .getSingleResult();

        Number databaseCount = (Number) result[0];
        Number userCount = (Number) result[1];

        return new AccountStatus.Builder()
                .setUserAccountId(userAccount.getId())
                .setTrial(true)
                .setUserCount(userCount.intValue())
                .setUserLimit(AccountStatus.FREE_TRIAL_LIMIT)
                .setLegacy(userAccount.getDateCreated() == null ||
                        userAccount.getDateCreated().before(new Date(1536624000000L)))
                .setDatabaseCount(databaseCount.intValue())
                .setExpirationTime(userAccount.getTrialEndDate())
                .build();

    }

    private AccountStatus queryBillingStatus(User userAccount) {

        Object[] result = (Object[]) entityManager.get().createNativeQuery(
                "SELECT count(distinct d.databaseId), count(distinct up.userId) " +
                        "FROM userlogin u " +
                        "LEFT JOIN userdatabase d ON (d.OwnerUserId=u.userId) " +
                        "LEFT JOIN userpermission up ON (up.databaseId=d.DatabaseId and up.allowview=1) " +
                        "WHERE u.billingAccountId = :accountId and d.dateDeleted IS NULL")
                .setParameter("accountId", userAccount.getBillingAccount().getId())
                .getSingleResult();

        Number databaseCount = (Number) result[0];
        Number userCount = (Number)result[1];


        return new AccountStatus.Builder()
                .setUserAccountId(userAccount.getId())
                .setTrial(false)
                .setDatabaseCount(databaseCount.intValue())
                .setUserCount(userCount.intValue())
                .setUserLimit(userAccount.getBillingAccount().getUserLimit())
                .setBillingAccountName(userAccount.getBillingAccount().getName())
                .setExpirationTime(userAccount.getBillingAccount().getEndTime())
                .build();

    }

    private boolean queryUserExistsInTrialAccount(int ownerUserId, int newUserId) {
        Number count = (Number) entityManager.get().createNativeQuery("SELECT count(*) FROM userdatabase d " +
                "LEFT JOIN userpermission up ON (up.databaseId = d.databaseId) " +
                "WHERE d.dateDeleted is null AND d.ownerUserId = :ownerId AND up.userId = :newUserId AND up.AllowView=1")
                .setParameter("ownerId", ownerUserId)
                .setParameter("newUserId", newUserId)
                .getSingleResult();

        return count.intValue() > 0;
    }


    private boolean queryUserExistsInBillingAccount(int billingAccountId, int newUserId) {
        Number count = (Number) entityManager.get().createNativeQuery(
                "SELECT count(*) FROM userlogin u " +
                "LEFT JOIN userdatabase d ON (d.ownerUserId = u.userId) " +
                "LEFT JOIN userpermission up ON (up.databaseId = d.databaseId) " +
                "WHERE u.billingAccountId = :billingAccountId AND d.dateDeleted IS NULL AND up.AllowView=1 AND up.userId = :newUserId")
                .setParameter("billingAccountId", billingAccountId)
                .setParameter("newUserId", newUserId)
                .getSingleResult();

        return count.intValue() > 0;
    }


    public void startFreeTrial(User user) {
        ZoneId zoneId = ZoneId.of( "Europe/Paris" );
        ZonedDateTime now = ZonedDateTime.ofInstant( Instant.now() , zoneId );
        ZonedDateTime endTime = now.plusDays( 30 );
        user.setTrialEndDate(Date.from(endTime.toInstant()));
    }

}
