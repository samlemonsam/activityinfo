package org.activityinfo.server.command.handler.sync;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.sync.server.JpaUpdateBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.server.database.hibernate.dao.HibernateDAOProvider;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.entity.*;
import org.hibernate.Session;
import org.json.JSONException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DbUpdateBuilder implements UpdateBuilder {

    private static final String REGION_PREFIX = "db/";

    private final UserDatabaseDAO userDatabaseDAO;
    private final EntityManager entityManager;

    private final Set<Integer> countryIds = Sets.newHashSet();
    private final List<Country> countries = Lists.newArrayList();
    private final List<AdminLevel> adminLevels = Lists.newArrayList();

    private UserDatabase database = null;

    private final Set<Integer> partnerIds = Sets.newHashSet();
    private final List<Partner> partners = Lists.newArrayList();
    private final ActivityEntities existingActivityEntities;

    private final Set<Integer> userIds = Sets.newHashSet();
    private final List<User> users = Lists.newArrayList();
    private final List<LocationType> locationTypes = Lists.newArrayList();
    private List<UserPermission> userPermissions;

    private static final Logger LOGGER = Logger.getLogger(DbUpdateBuilder.class.getName());

    private final List<Project> projects = Lists.newArrayList();

    @Inject
    public DbUpdateBuilder(EntityManagerFactory entityManagerFactory) {
        // create a new, unfiltered entity manager so we can see deleted records
        this.entityManager = entityManagerFactory.createEntityManager();
        this.userDatabaseDAO = HibernateDAOProvider.makeImplementation(UserDatabaseDAO.class,
                UserDatabase.class,
                entityManager);
        this.existingActivityEntities = new ActivityEntities(entityManager);
    }

    @SuppressWarnings("unchecked") @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException {

        try {
            // get the permissions before we apply the filter
            // otherwise they will be excluded
            userPermissions = entityManager.createQuery("select p from UserPermission p where p.user.id = ?1")
                                           .setParameter(1, user.getId())
                                           .getResultList();

            DomainFilters.applyUserFilter(user, entityManager);

            int dbId = parseDbId(request);

            database = userDatabaseDAO.findById(dbId);

            Preconditions.checkNotNull(database, "Failed to fetch database by id:" + dbId + ", region: " + request);

            long localVersion = request.getLocalVersion() == null ? 0 : Long.parseLong(request.getLocalVersion());
            long serverVersion = getCurrentDbVersion();

            LOGGER.info("Schema versions: local = " + localVersion + ", server = " + serverVersion);

            SyncRegionUpdate update = new SyncRegionUpdate();
            update.setVersion(Long.toString(serverVersion));
            update.setComplete(true);

            if (localVersion < serverVersion) {
                makeEntityLists();
                update.setSql(buildSql());
            }
            return update;
        } finally {
            entityManager.close();
        }
    }

    private int parseDbId(GetSyncRegionUpdates request) {
        if (!request.getRegionId().startsWith(REGION_PREFIX)) {
            throw new AssertionError("Expected region prefixed by '" + REGION_PREFIX +
                    "', got '" + request.getRegionId() + "'");
        }
        return Integer.parseInt(request.getRegionId().substring(REGION_PREFIX.length()));
    }

    private String buildSql() throws JSONException {
        JpaUpdateBuilder builder = new JpaUpdateBuilder();

        buildCleanUpSql(builder);

        builder.insert(" or replace ", Country.class, countries);
        builder.insert(" or replace ", AdminLevel.class, adminLevels);
        builder.insert(" or replace ", Partner.class, partners);
        builder.insert(" or replace ", UserDatabase.class, Lists.newArrayList(database));
        builder.insert(" or replace ", Activity.class, Lists.newArrayList(existingActivityEntities.getActivitiesMap().values()));
        builder.insert(" or replace ", Indicator.class, Lists.newArrayList(existingActivityEntities.getIndicators().values()));
        builder.insert(" or replace ", AttributeGroup.class, Lists.newArrayList(existingActivityEntities.getAttributeGroups().values()));
        builder.insert(" or replace ", Attribute.class, Lists.newArrayList(existingActivityEntities.getAttributes().values()));
        builder.insert(" or replace ", LocationType.class, locationTypes);
        builder.insert(" or replace ", User.class, users);
        builder.insert(" or replace ", UserPermission.class, userPermissions);
        builder.insert(" or replace ", Project.class, projects);
        builder.insert(" or replace ", LockedPeriod.class, existingActivityEntities.getAllLockedPeriods());

        createAndSyncIndicatorlinks(builder);
        createAndSyncPartnerInDatabase(builder);
        createAndSyncAttributeGroupInActivity(builder);


        return builder.asJson();
    }

    private void buildCleanUpSql(JpaUpdateBuilder builder) throws JSONException {

        ((Session)entityManager.getDelegate()).disableFilter("hideDeleted");

        List<Activity> deletedActivities = entityManager.createQuery("select a from Activity a where a.database.id = ?1 and a.dateDeleted is not null")
                .setParameter(1, database.getId())
                .getResultList();

        ActivityEntities allActivities = new ActivityEntities(entityManager);
        allActivities.collect(deletedActivities);
        allActivities.collect(existingActivityEntities);

        Set<Integer> attributesIds = allActivities.getAttributes().keySet();
        Set<Integer> indicatorIds = allActivities.getIndicators().keySet();
        Set<Integer> attributeGroupIds = allActivities.getAttributeGroups().keySet();
        Set<Integer> activityIds = allActivities.getActivitiesMap().keySet();

        if (!attributesIds.isEmpty()) {
            builder.executeStatement("delete from attributevalue where attributeid in " + SqlQueryUtil.idSet(attributesIds));
        }

        if (!indicatorIds.isEmpty()) {
            String idSet = SqlQueryUtil.idSet(indicatorIds);
            builder.executeStatement("delete from indicatorvalue where indicatorid in " + idSet);
            builder.executeStatement("delete from indicatorlink where sourceindicatorid in " + idSet +
                    " or destinationindicatorid in " + idSet);

            List<IndicatorValue> indicatorValues = entityManager.createQuery("select i from IndicatorValue i where i.indicator.id in " + idSet)
                    .getResultList();
            if (!indicatorValues.isEmpty()) {
                Set<Integer> reportingPeriods = Sets.newHashSet();
                for (IndicatorValue value : indicatorValues) {
                    reportingPeriods.add(value.getReportingPeriod().getId());
                }

                if (!reportingPeriods.isEmpty()) {
                    builder.executeStatement("delete from reportingperiod where reportingperiodid in " + SqlQueryUtil.idSet(reportingPeriods));
                }
            }
        }

        if (!attributeGroupIds.isEmpty()) {
            String idSet = SqlQueryUtil.idSet(attributeGroupIds);
            builder.executeStatement("delete from attribute where attributegroupid in " + idSet);
            builder.executeStatement("delete from attributegroup where attributegroupid in " + idSet);
        }

        if (!activityIds.isEmpty()) {
            String idSet = SqlQueryUtil.idSet(activityIds);
            builder.executeStatement("delete from attributegroupinactivity where activityid in " + idSet);
            builder.executeStatement("delete from indicator where activityid in " + idSet);
            builder.executeStatement("delete from site where activityid in " + idSet);
        }

        builder.executeStatement("delete from project where databaseid = " + database.getId());
        builder.executeStatement("delete from activity where databaseid = " + database.getId());

        // enabled deleted filter back
        ((Session)entityManager.getDelegate()).enableFilter("hideDeleted");
    }

    private void createAndSyncIndicatorlinks(JpaUpdateBuilder builder) throws JSONException {

        if (!existingActivityEntities.getIndicatorLinks().isEmpty()) {
            builder.beginPreparedStatement(
                    "insert or replace into IndicatorLink (SourceIndicatorId, DestinationIndicatorId) values (?, ?) ");
            for (IndicatorLinkEntity il : existingActivityEntities.getIndicatorLinks()) {
                builder.addExecution(il.getId().getSourceIndicatorId(), il.getId().getDestinationIndicatorId());
            }
            builder.finishPreparedStatement();
        }
    }

    private void createAndSyncPartnerInDatabase(JpaUpdateBuilder builder) throws JSONException {
        // do not clear table, now we handle data per db
//        builder.executeStatement("delete from PartnerInDatabase");

        if (anyPartners()) {
            builder.beginPreparedStatement("insert or replace into PartnerInDatabase (DatabaseId, PartnerId) values (?, ?) ");

            for (Partner partner : database.getPartners()) {
                builder.addExecution(database.getId(), partner.getId());
            }
            builder.finishPreparedStatement();
        }
    }

    private void createAndSyncAttributeGroupInActivity(JpaUpdateBuilder builder) throws JSONException {

        // do not clear table, now we handle data per db
//        builder.executeStatement("delete from AttributeGroupInActivity");

        if (anyAttributes()) {
            builder.beginPreparedStatement(
                    "insert or replace into AttributeGroupInActivity (ActivityId, AttributeGroupId) values (?,?)");

            for (Activity activity : database.getActivities()) {
                for (AttributeGroup group : activity.getAttributeGroups()) {
                    builder.addExecution(activity.getId(), group.getId());
                }
            }

            builder.finishPreparedStatement();
        }
    }

    private boolean anyPartners() {
        return !database.getPartners().isEmpty();
    }

    private boolean anyAttributes() {
        for (Activity activity : database.getActivities()) {
            if (!activity.getAttributeGroups().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void makeEntityLists() {

        if (!userIds.contains(database.getOwner().getId())) {
            User u = database.getOwner();
            // don't send hashed password to client
            // EEK i think hibernate will persist this to the database
            // automatically if we change it here!!
            // u.setHashedPassword("");
            users.add(u);
            userIds.add(u.getId());
        }

        if (!countryIds.contains(database.getCountry().getId())) {
            countries.add(database.getCountry());
            adminLevels.addAll(database.getCountry().getAdminLevels());
            countryIds.add(database.getCountry().getId());
            for (org.activityinfo.server.database.hibernate.entity.LocationType l : database.getCountry()
                    .getLocationTypes()) {
                locationTypes.add(l);
            }
        }
        for (Partner partner : database.getPartners()) {
            if (!partnerIds.contains(partner.getId())) {
                partners.add(partner);
                partnerIds.add(partner.getId());
            }
        }

        projects.addAll(Lists.newArrayList(database.getProjects()));

        existingActivityEntities.getAllLockedPeriods().addAll(database.getLockedPeriods());
        for (Project project : database.getProjects()) {
            existingActivityEntities.getAllLockedPeriods().addAll(project.getLockedPeriods());
        }

        existingActivityEntities.collect(database.getActivities());

    }

    public long getCurrentDbVersion() {
        long currentVersion = database.getVersion();
        for (UserPermission perm : userPermissions) {
            if (perm.getVersion() > currentVersion) {
                currentVersion = perm.getVersion();
            }
        }
        return currentVersion;
    }
}
