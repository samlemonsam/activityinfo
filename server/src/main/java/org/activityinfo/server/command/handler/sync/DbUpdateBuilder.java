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

import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.*;
import org.json.JSONException;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.logging.Logger;

public class DbUpdateBuilder implements UpdateBuilder {

    private static final Logger LOGGER = Logger.getLogger(DbUpdateBuilder.class.getName());

    private static final int MINIMUM_DB_VERSION = 1;
    private static final String REGION_PREFIX = "db/";

    private final EntityManager entityManager;
    private final PermissionOracle permissionOracle;

    private JpaBatchBuilder batch;
    private UserDatabase database;
    private UserPermission permission;

    @Inject
    public DbUpdateBuilder(EntityManager entityManager, PermissionOracle permissionOracle) {
        this.entityManager = entityManager;
        this.permissionOracle = permissionOracle;
    }

    @SuppressWarnings("unchecked") @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException, IOException {

        // get the permissions before we apply the filter
        // otherwise they will be excluded

        int dbId = parseDbId(request);

        this.database = entityManager.find(UserDatabase.class, dbId);
        this.permission = permissionOracle.getPermissionByUser(database, user);

        Preconditions.checkNotNull(database, "Failed to fetch database by id:" + dbId + ", region: " + request);

        // This database's version is a function of both the database's version
        // and our permission own version, which determines whether we can see the database or not
        long localVersion = request.getLocalVersion() == null ? 0 : Long.parseLong(request.getLocalVersion());

        long serverVersion = Math.max(MINIMUM_DB_VERSION,
                Math.max(database.getVersion(), permission.getVersion()));


        LOGGER.info("Schema versions: local = " + localVersion + ", server = " + serverVersion);

        SyncRegionUpdate update = new SyncRegionUpdate();
        update.setVersion(Long.toString(serverVersion));
        update.setComplete(true);

        if (localVersion < serverVersion) {
            update.setSql(buildSql());
        }
        return update;
    }

    private int parseDbId(GetSyncRegionUpdates request) {
        if (!request.getRegionId().startsWith(REGION_PREFIX)) {
            throw new AssertionError("Expected region prefixed by '" + REGION_PREFIX +
                    "', got '" + request.getRegionId() + "'");
        }
        return Integer.parseInt(request.getRegionId().substring(REGION_PREFIX.length()));
    }

    private String buildSql() throws JSONException, IOException {
        batch = new JpaBatchBuilder(entityManager);

        delete(UserDatabase.class, inDatabase());
        insert(UserDatabase.class, inDatabase());

        // Projects
        delete(Project.class, inDatabase());
        insert(Project.class, notDeletedAnd(inDatabase()));

        delete(Activity.class, inDatabase());
        insert(Activity.class, notDeletedAnd(inDatabase()));

        delete(Indicator.class, inDatabaseActivities());
        insert(Indicator.class, notDeletedAnd(inDatabaseActivities(notDeleted())));


        // Attribute Groups
        delete(AttributeGroup.class, inDatabaseAttributeGroups());
        insert(AttributeGroup.class, notDeletedAnd(inDatabaseAttributeGroups()));

        delete(Attribute.class, inDatabaseAttributeGroups());
        insert(Attribute.class, notDeletedAnd(inDatabaseAttributeGroupsNotDeleted()));

        delete(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, inDatabaseAttributeGroups());
        insert(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, inDatabaseAttributeGroupsNotDeleted());

        // Locked Periods
        delete(LockedPeriod.class, lockedPeriodsInDatabase());
        insert(LockedPeriod.class, lockedPeriodsInDatabase(notDeleted()));


        // Since Partners are shared, we replace the join table completely
        // for this database, but do not delete partners removed
        // from the database as they may be used by other databases
        insert(Partner.class, partnersInDatabase());
        delete(Tables.PARTNER_IN_DATABASE, inDatabase());
        insert(Tables.PARTNER_IN_DATABASE, inDatabase());

        insert(User.class, "userId = " + database.getOwner().getId());
        insert(UserPermission.class, "userId = " + permission.getUser().getId());

        return batch.build();
    }

    private String inDatabaseAttributeGroups(String... criteria) {
        return  "attributeGroupId IN (SELECT attributeGroupId from AttributeGroupInActivity where " +
                inDatabaseActivities(criteria) + ")";
    }

    private String inDatabaseAttributeGroupsNotDeleted() {
        return "AttributeGroupId in (SELECT g.attributeGroupId from AttributeGroup g WHERE " +
                    "g.DateDeleted IS NULL AND " + inDatabaseAttributeGroups(notDeleted()) + ")";
    }

    private String lockedPeriodsInDatabase(String... criteria) {
        return  "UserDatabaseId = " + database.getId() + " OR " +
                 inProjectDatabases(criteria) + " OR " +
                 inDatabaseActivities(criteria);
    }

    private String inProjectDatabases(String... criteria) {
        return "ProjectId IN (SELECT ProjectID FROM Project WHERE " +
                all(inDatabase(), criteria) + ")";
    }

    private String inDatabase() {
        return "DatabaseId = " + database.getId();
    }

    private String notDeleted() {
        return "DateDeleted IS NULL";
    }

    private String notDeletedAnd(String criteria) {
        return "DateDeleted IS NULL AND " + criteria;
    }

    private String inDatabaseActivities(String... criteria) {
        return  "ActivityId IN (SELECT ActivityId from activity where " +
                all(inDatabase(), criteria) + ")";
    }

    private String all(String criteria, String[] extraCriteria) {
        if(extraCriteria.length == 0) {
            return criteria;
        } else {
            assert extraCriteria.length == 1;
            return criteria + " AND " + extraCriteria[0];
        }
    }

    private String partnersInDatabase() {
        return "partnerId in (select partnerId From PartnerInDatabase where DatabaseId = " + database.getId() + ")";
    }


    private void delete(String tableName, String criteria) throws IOException {
        batch.addStatement("DELETE FROM " + tableName + " WHERE " + criteria);
    }


    private void delete(Class<?> entity, String criteria) throws IOException {
        batch.delete(entity, criteria);
    }

    private void insert(Class<?> entityClass, String criteria) {
        if(permission.isAllowView() && !database.isDeleted()) {
            LOGGER.severe(entityClass.getName() + " Criteria: " + criteria.toLowerCase());
            batch.insert(entityClass, criteria.toLowerCase());
        }
    }

    private void insert(String tableName, String criteria) {
        if(permission.isAllowView() && !database.isDeleted()) {
            SqlQuery query = SqlQuery.selectAll().from(tableName).whereTrue(criteria.toLowerCase());
            LOGGER.severe(query.sql());
            batch.insert()
                    .into(tableName)
                    .from(query)
                    .execute(entityManager);
        }
    }

}
