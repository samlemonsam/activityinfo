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
package org.activityinfo.server.command.handler.sync;

import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.*;
import org.activityinfo.store.spi.DatabaseProvider;
import org.json.JSONException;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class DbUpdateBuilder implements UpdateBuilder {
    public static final String REGION_TYPE = "db";

    private static final Logger LOGGER = Logger.getLogger(DbUpdateBuilder.class.getName());
    private static final HashFunction HASHER = Hashing.murmur3_128();
    
    private static final long MINIMUM_DB_VERSION = 1L;

    private final EntityManager entityManager;
    private final DatabaseProvider provider;

    private JpaBatchBuilder batch;
    private Database database;
    private Optional<UserDatabaseMeta> databaseMeta;

    @Inject
    public DbUpdateBuilder(EntityManager entityManager,
                           DatabaseProvider databaseProvider) {
        this.entityManager = entityManager;
        this.provider = databaseProvider;
    }

    @SuppressWarnings("unchecked") @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException, IOException {

        // get the permissions before we apply the filter
        // otherwise they will be excluded
        this.database = entityManager.find(Database.class, request.getRegionId());
        this.databaseMeta = provider.getDatabaseMetadata(CuidAdapter.databaseId(request.getRegionId()), user.getId());

        Preconditions.checkNotNull(database, "Failed to fetch database by id:" +
                request.getRegionId() + ", region: " + request);
        Preconditions.checkArgument(databaseMeta.isPresent(), "Failed to fetch databaseMeta by id:" +
                request.getRegionId() + ", region: " + request);

        // This database's version is a function of both the database's version
        // and our permission own version, which determines whether we can see the database or not
        long localVersion = request.getLocalVersion() == null ? 0L : Long.parseLong(request.getLocalVersion());

        long serverDbVersion = Math.max(database.getVersion(), MINIMUM_DB_VERSION);
        long serverUserVersion = databaseMeta.get().getUserVersion();
        long serverVersion = HASHER.hashLong(serverDbVersion + serverUserVersion).asLong();

        LOGGER.info(() -> String.format("Schema versions: local = %d, server = %d", localVersion, serverVersion));

        batch = new JpaBatchBuilder(entityManager, request.getRegionPath());
        batch.setVersion(serverVersion);
        batch.setComplete(true);

        if (localVersion != serverVersion) {
            queryUpdates(user.getId());
        }

        return batch.buildUpdate();
    }

    private void queryUpdates(int userId) throws IOException {

        delete(Database.class, inDatabase());
        insert(Database.class, inDatabase());

        // Projects
        delete(Project.class, inDatabase());
        insert(Project.class, notDeletedAnd(inDatabase()));

        // Folders
        delete(Folder.class, inDatabase());
        insert(Folder.class, inDatabase());

        delete(Activity.class, inDatabase());
        insert(Activity.class, notDeletedAnd(inDatabase()));

        delete(Indicator.class, inDatabaseActivities());
        insertIndicators();

        // Attribute Groups
        delete(AttributeGroup.class, inDatabaseAttributeGroups());
        insertAttributeGroups();

        delete(Attribute.class, inDatabaseAttributeGroups());
        insertAttributes();

        delete(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, inDatabaseAttributeGroups());
        insert(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, inDatabaseAttributeGroupsNotDeleted());

        // Locked Periods
        delete(LockedPeriod.class, lockedPeriodsInDatabase());
        insert(LockedPeriod.class, lockedPeriodsInDatabase());


        // Since Partners are model, we replace the join table completely
        // for this database, but do not delete partners removed
        // from the database as they may be used by other databases
        insertPartners();
        delete(Tables.PARTNER_IN_DATABASE, inDatabase());
        insert(Tables.PARTNER_IN_DATABASE, inDatabase());

        insert(User.class, "userId = " + database.getOwner().getId());
        insert(UserPermission.class, "userId = " + userId);

        delete(Tables.GROUP_ASSIGNMENT, inDatabaseUserPermission(userId));
        insert(Tables.GROUP_ASSIGNMENT, inDatabaseUserPermission(userId));
    }

    private void insertIndicators() {
        batch.insert(Indicator.class)
                .join(Activity.class)
                .whereNotDeleted(Indicator.class)
                .whereNotDeleted(Activity.class)
                .where(inDatabase())
                .execute();
        
    }

    private void insertAttributeGroups() {
        batch.insert(AttributeGroup.class)
                .join(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, "AttributeGroupId")
                .join(Activity.class)
                .whereNotDeleted(AttributeGroup.class)
                .whereNotDeleted(Activity.class)
                .where(inDatabase())
                .execute();

    }

    private void insertAttributes() {
        batch.insert(Attribute.class)
                .join(AttributeGroup.class)
                .join(Tables.ATTRIBUTE_GROUP_IN_ACTIVITY, "AttributeGroupId")
                .join(Activity.class)
                .whereNotDeleted(Attribute.class)
                .whereNotDeleted(AttributeGroup.class)
                .whereNotDeleted(Activity.class)
                .where(inDatabase())
                .execute();
    }
    
    private void insertPartners() {
        batch.insert(Partner.class)
                .join(Tables.PARTNER_IN_DATABASE, "PartnerId")
                .where(inDatabase())
                .execute();
    }

    private String inDatabaseAttributeGroups(String... criteria) {
        return  "attributeGroupId IN (SELECT attributeGroupId from AttributeGroupInActivity where " +
                inDatabaseActivities(criteria) + ")";
    }

    private String inDatabaseAttributeGroupsNotDeleted() {
        return "AttributeGroupId in (SELECT g.attributeGroupId from AttributeGroup g WHERE " +
                    "g.DateDeleted IS NULL AND " + inDatabaseAttributeGroups(notDeleted()) + ")";
    }

    private String lockedPeriodsInDatabase() {
        return  "databaseId = " + database.getId();
    }

    private String inDatabase() {
        return "DatabaseId = " + database.getId();
    }

    private String inDatabaseUserPermission(int userId) {
        return "UserPermissionId IN (SELECT up.UserPermissionId FROM userpermission up " +
                "WHERE up.DatabaseId=" + database.getId() + " AND up.UserId=" + userId + ")";
    }

    @SuppressWarnings("squid:S3400")
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


    private void delete(String tableName, String criteria) throws IOException {
        batch.addStatement("DELETE FROM " + tableName + " WHERE " + criteria);
    }


    private void delete(Class<?> entity, String criteria) throws IOException {
        batch.delete(entity, criteria);
    }

    private void insert(Class<?> entityClass, String criteria) {
        if(!database.isDeleted() && databaseMeta.isPresent() && PermissionOracle.canSyncClassicDatabase(databaseMeta.get())) {
            LOGGER.fine(() -> entityClass.getName() + " Criteria: " + criteria.toLowerCase());
            batch.insert(entityClass, criteria.toLowerCase());
        }
    }

    private void insert(String tableName, String criteria) {
        if(!database.isDeleted() && databaseMeta.isPresent() && PermissionOracle.canSyncClassicDatabase(databaseMeta.get())) {
            SqlQuery query = SqlQuery.selectAll().from(tableName).whereTrue(criteria.toLowerCase());
            LOGGER.fine(query.sql());
            batch.insert()
                    .into(tableName)
                    .from(query)
                    .execute(entityManager);
        }
    }

}
