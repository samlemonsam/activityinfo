package org.activityinfo.server.endpoint.rest;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class BillingAccountOracle {

    private final Logger LOGGER = Logger.getLogger(BillingAccountOracle.class.getName());

    private final Provider<EntityManager> entityManager;

    private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();

    /**
     * The duration to cache the suspension status of suspended databases. We use a shorter cache time to ensure
     * that if the customer extends the effect is more or less immediate.
     */
    private static final Expiration SUSPENDED_CACHE_EXPIRATION = Expiration.byDeltaSeconds((int) TimeUnit.MINUTES.toSeconds(10));

    /**
     * The duration to cache the suspension status of active databases.
     */
    private static final Expiration ACTIVE_CACHE_EXPIRATION = Expiration.byDeltaSeconds((int) TimeUnit.HOURS.toSeconds(12));


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
        } else {
            return queryTrialStatus(userAccount);
        }
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
    public boolean isAllowAddUser(@Nullable User newUser, Database database) {

        AccountStatus accountStatus = getStatus(database.getOwner());
        if(accountStatus.getUserCount() < accountStatus.getUserLimit()) {
            return true;
        }

        // If the user doesn't exist yet, then we are definitely over our limit
        if(newUser == null) {
            return false;
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

        Date trialEndDate = userAccount.getTrialEndDate();
        if(trialEndDate == null) {
            trialEndDate = new LocalDate(2999, 1, 1).atMidnightInMyTimezone();
        }

        return new AccountStatus.Builder()
                .setUserAccountId(userAccount.getId())
                .setTrial(true)
                .setUserCount(userCount.intValue())
                .setUserLimit(AccountStatus.FREE_TRIAL_LIMIT)
                .setLegacy(userAccount.getDateCreated() == null ||
                        userAccount.getDateCreated().before(new Date(1536624000000L)))
                .setDatabaseCount(databaseCount.intValue())
                .setExpirationTime(trialEndDate)
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
                .setExpectedPaymentDate(userAccount.getBillingAccount().getExpectedPaymentDate())
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


    public boolean isDatabaseSuspended(int databaseId) {
        return getSuspendedDatabases(Collections.singleton(databaseId)).contains(databaseId);
    }

    private String memcacheDatabaseKey(int databaseId) {
        return "db-suspended:" + databaseId;
    }

    /**
     * Given a set of database ids, determine which ones, if any are suspended.
     * @param databaseIds
     * @return the set of ids of databases that are suspended
     */
    public Set<Integer> getSuspendedDatabases(Set<Integer> databaseIds) {

        LOGGER.info("Checking suspension flag for " + databaseIds.size() + "...");

        if(databaseIds.isEmpty()) {
            return Collections.emptySet();
        }

        Map<String, Object> cachedStatus;
        try {
            cachedStatus = memcache.getAll(databaseIds
                    .stream()
                    .map(this::memcacheDatabaseKey)
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to query memcache", e);
            cachedStatus = Collections.emptyMap();
        }

        Set<Integer> suspendedSet = new HashSet<>();

        Set<Integer> toQuery = new HashSet<>();

        for (Integer databaseId : databaseIds) {
            String databaseKey = memcacheDatabaseKey(databaseId);
            if(cachedStatus.containsKey(databaseKey)) {
                Boolean suspended = (Boolean) cachedStatus.get(databaseKey);
                if(suspended) {
                    suspendedSet.add(databaseId);
                }
            } else {
                toQuery.add(databaseId);
            }
        }

        if(!toQuery.isEmpty()) {
            suspendedSet.addAll(querySuspendedDatabases(toQuery));
        }

        return suspendedSet;
    }

    private Set<Integer> querySuspendedDatabases(Set<Integer> databaseIds) {

        LOGGER.info("Querying suspension flag for " + databaseIds.size() + "...");

        if(databaseIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<Number> suspendedDatabaseList = entityManager.get().createNativeQuery(
                "SELECT d.databaseId " +
                        "FROM userdatabase d " +
                        "LEFT JOIN userlogin u ON (d.OwnerUserId=u.userId) " +
                        "WHERE d.databaseId IN :databases AND u.billingAccountId IS NULL AND u.trialEndDate < NOW()")
                .setParameter("databases", databaseIds)
                .getResultList();

        Set<Integer> suspendedSet = suspendedDatabaseList
                .stream()
                .map(n -> n.intValue())
                .collect(Collectors.toSet());

        Map<String, Object> toCacheActive = new HashMap<>();
        Map<String, Object> toCacheSuspended = new HashMap<>();

        for (Integer databaseId : databaseIds) {
            boolean suspended = suspendedDatabaseList.contains(databaseId);
            if(suspended) {
                toCacheSuspended.put(memcacheDatabaseKey(databaseId), suspended);
            } else {
                toCacheActive.put(memcacheDatabaseKey(databaseId), suspended);
            }
        }

        try {
            if(!toCacheActive.isEmpty()) {
                memcache.putAll(toCacheActive, ACTIVE_CACHE_EXPIRATION);
            }
            if(!toCacheSuspended.isEmpty()) {
                memcache.putAll(toCacheSuspended, SUSPENDED_CACHE_EXPIRATION);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Memcache store failed", e);
        }

        return suspendedSet;
    }

    public Set<ResourceId> getSuspendedDatabasesById(Set<ResourceId> databaseIds) {

        Set<Integer> integerIds = getSuspendedDatabases(databaseIds
                .stream()
                .filter(id -> id.getDomain() == CuidAdapter.DATABASE_DOMAIN)
                .map(id -> CuidAdapter.getLegacyIdFromCuid(id))
                .collect(Collectors.toSet()));

        return integerIds.stream().map(id -> CuidAdapter.databaseId(id)).collect(Collectors.toSet());
    }
}
