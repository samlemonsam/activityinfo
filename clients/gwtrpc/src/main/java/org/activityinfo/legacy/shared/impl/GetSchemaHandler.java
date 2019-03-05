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
package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.bedatadriven.rebar.sql.client.util.RowHandler;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.json.Json;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.database.UserPermissionModel;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.promise.Promise;

import java.util.*;

public class GetSchemaHandler implements CommandHandlerAsync<GetSchema, SchemaDTO> {

    @Override
    public void execute(GetSchema command, ExecutionContext context, AsyncCallback<SchemaDTO> callback) {

        new SchemaBuilder().build(context, callback);
    }

    private interface SchemaFilter {

        boolean isVisible(FolderDTO folder);

        boolean isVisible(ActivityDTO activity);

        boolean hasFolderLimitations();
    }

    private static final SchemaFilter UNFILTERED = new SchemaFilter() {
        @Override
        public boolean isVisible(FolderDTO folder) {
            return true;
        }

        @Override
        public boolean isVisible(ActivityDTO activity) {
            return true;
        }

        @Override
        public boolean hasFolderLimitations() {
            return false;
        }
    };

    private static class FolderFilter implements SchemaFilter {

        private Set<Integer> folders = new HashSet<>();

        private FolderFilter(String modelJson) {
            UserPermissionModel model = UserPermissionModel.fromJson(Json.parse(modelJson));
            for (GrantModel grantModel : model.getGrants()) {
                if(grantModel.getResourceId().getDomain() == CuidAdapter.FOLDER_DOMAIN) {
                    int folderId = CuidAdapter.getLegacyIdFromCuid(grantModel.getResourceId());
                    folders.add(folderId);
                }
            }
        }

        @Override
        public boolean isVisible(FolderDTO folder) {
            return folders.contains(folder.getId());
        }

        @Override
        public boolean isVisible(ActivityDTO activity) {
            if(activity.getFolder() == null) {
                return false;
            }
            return folders.contains(activity.getFolder().getId());
        }

        @Override
        public boolean hasFolderLimitations() {
            return true;
        }


    }

    private class SchemaBuilder {
        private final List<UserDatabaseDTO> databaseList = new ArrayList<>();
        private final List<CountryDTO> countryList = new ArrayList<>();

        private final Map<Integer, UserDatabaseDTO> databaseMap = new HashMap<>();
        private final Map<Integer, FolderDTO> folders = new HashMap<>();
        private final Map<Integer, CountryDTO> countries = new HashMap<>();
        private final Map<Integer, PartnerDTO> partners = new HashMap<>();
        private final Map<Integer, ActivityDTO> activities = new HashMap<>();
        private final Map<Integer, ProjectDTO> projects = new HashMap<>();
        private final Map<Integer, LocationTypeDTO> locationTypes = new HashMap<>();

        private final Map<Integer, SchemaFilter> databaseFilters = new HashMap<>();

        private SqlTransaction tx;
        private ExecutionContext context;

        public Promise<Void> loadCountries() {
            return execute(SqlQuery.select()
                            .appendColumn("CountryId", "id")
                            .appendColumn("Name", "name")
                            .appendColumn("X1", "x1")
                            .appendColumn("y1", "y1")
                            .appendColumn("x2", "x2")
                            .appendColumn("y2", "y2")
                            .appendColumn("ISO2", "ISO2")
                            .from("country"),
                    new RowHandler() {
                        @Override
                        public void handleRow(SqlResultSetRow rs) {
                            CountryDTO country = new CountryDTO();
                            country.setId(rs.getInt("id"));
                            country.setName(rs.getString("name"));
                            country.setCodeISO(rs.getString("ISO2"));

                            Extents bounds = new Extents(rs.getDouble("y1"),
                                    rs.getDouble("y2"),
                                    rs.getDouble("x1"),
                                    rs.getDouble("x2"));

                            country.setBounds(bounds);

                            countries.put(country.getId(), country);
                            countryList.add(country);
                        }
                    });
        }

        public Promise<Void> loadLocationTypes() {
            SqlQuery query = SqlQuery.select("locationTypeId",
                    "name",
                    "boundAdminLevelId",
                    "DateDeleted",
                    "countryId",
                    "workflowId",
                    "databaseId").from("locationtype");

            return execute(query, new RowHandler() {

                @Override
                public void handleRow(SqlResultSetRow row) {
                    LocationTypeDTO type = new LocationTypeDTO();
                    type.setId(row.getInt("locationTypeId"));
                    type.setName(row.getString("name"));
                    type.setWorkflowId(row.getString("workflowId"));

                    if (!row.isNull("databaseId")) {
                        type.setDatabaseId(row.getInt("databaseId"));
                    }

                    if (!row.isNull("boundAdminLevelId")) {
                        type.setBoundAdminLevelId(row.getInt("boundAdminLevelId"));
                    }

                    type.setDeleted(!row.isNull("DateDeleted"));

                    int countryId = row.getInt("countryId");
                    CountryDTO country = countries.get(countryId);
                    country.getLocationTypes().add(type);

                    type.setAdminLevels(levelsForLocationType(country, type));
                    type.setCountryBounds(country.getBounds());

                    locationTypes.put(type.getId(), type);
                }
            });
        }

        private List<AdminLevelDTO> levelsForLocationType(CountryDTO country, LocationTypeDTO type) {

            if (type.isAdminLevel()) {
                // if this activity is bound to an administrative
                // level, then we need only as far down as this goes
                return country.getAdminLevelAncestors(type.getBoundAdminLevelId());

            } else if (type.isNationwide()) {
                return Lists.newArrayList();

            } else {
                // all admin levels
                return country.getAdminLevels();
            }
        }

        public Promise<Void> loadAdminLevels() {
            return execute(SqlQuery.select("adminLevelId", "name", "parentId", "countryId")
                            .from("adminlevel")
                            .whereTrue("deleted=0"),
                    new RowHandler() {

                        @Override
                        public void handleRow(SqlResultSetRow row) {
                            AdminLevelDTO level = new AdminLevelDTO();
                            level.setId(row.getInt("adminLevelId"));
                            level.setName(row.getString("name"));
                            level.setCountryId(row.getInt("countryId"));

                            if (!row.isNull("parentId")) {
                                level.setParentLevelId(row.getInt("parentId"));
                            }

                            countries.get(level.getCountryId()).getAdminLevels().add(level);
                        }
                    });
        }

        public Promise<Void> loadDatabases() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery query = SqlQuery.select("d.DatabaseId")
                    .appendColumn("d.Name")
                    .appendColumn("d.FullName")
                    .appendColumn("d.OwnerUserId")
                    .appendColumn("d.CountryId")
                    .appendColumn("d.TransferToken IS NOT NULL", "pendingTransfer")
                    .appendColumn("d.TransferUser", "transferUserId")
                    .appendColumn("o.Name", "OwnerName")
                    .appendColumn("o.Email", "OwnerEmail")
                    .appendColumn("p.AllowViewAll", "allowViewAll")
                    .appendColumn("p.allowCreate", "allowCreate")
                    .appendColumn("p.allowCreateAll", "allowCreateAll")
                    .appendColumn("p.AllowEdit", "allowEdit")
                    .appendColumn("p.AllowEditAll", "allowEditAll")
                    .appendColumn("p.allowDelete", "allowDelete")
                    .appendColumn("p.allowDeleteAll", "allowDeleteAll")
                    .appendColumn("p.AllowManageUsers", "allowManageUsers")
                    .appendColumn("p.AllowManageAllUsers", "allowManageAllUsers")
                    .appendColumn("p.AllowDesign", "allowDesign")
                    .appendColumn("p.AllowExport", "allowExport")
                    .appendColumn("p.model", "permissionsModel")
                    .from("userdatabase d")
                    .leftJoin(SqlQuery.selectAll()
                            .from("userpermission")
                            .where("userpermission.UserId")
                            .equalTo(context.getUser().getId()), "p")
                            .on("p.DatabaseId = d.DatabaseId")
                    .leftJoin("userlogin o").on("d.OwnerUserId = o.UserId")
                    .where("d.DateDeleted")
                    .isNull()
                    .orderBy("d.Name");

            if(context.isRemote()) {
                query.leftJoin("billingaccount ba").on("ba.id=o.billingAccountId");
                query.appendColumn("ba.name", "baName");
                query.appendColumn("ba.endTime", "baEndDate");
                query.appendColumn("o.trialEndDate", "trialEndDate");
            }

            // this is quite hackesh. we ultimately need to split up GetSchema()
            // into
            // GetDatabases() and GetDatabaseSchema() so that the client has
            // more fine-grained
            // control over which databases are visible, which will be important
            // as the number
            // of public databases grow
            if (context.getUser().isAnonymous()) {
                query.whereTrue("(d.DatabaseId in (select pa.DatabaseId from activity pa where pa.published>0))");
            } else {
                query.whereTrue("(o.userId = ? or p.AllowView = 1)").appendParameter(context.getUser().getId());
            }

            query.execute(tx, new SqlResultCallback() {

                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet results) {

                    LocalDate today = new LocalDate();

                    for (SqlResultSetRow row : results.getRows()) {
                        UserDatabaseDTO db = new UserDatabaseDTO();
                        db.setId(row.getInt("DatabaseId"));
                        db.setName(row.getString("Name"));
                        db.setFullName(row.getString("FullName"));
                        db.setAmOwner(row.getInt("OwnerUserId") == context.getUser().getId());
                        db.setCountry(countries.get(row.getInt("CountryId")));
                        db.setOwnerName(row.getString("OwnerName"));
                        db.setOwnerEmail(row.getString("OwnerEmail"));

                        if(context.isRemote()) {
                            if(row.isNull("baName")) {
                                db.setBillingAccountName("Free Trial Account");
                                LocalDate trialEndDate;
                                if(!row.isNull("trialEndDate")) {
                                    trialEndDate = new LocalDate(row.getDate("trialEndDate"));
                                } else {
                                    trialEndDate = new LocalDate(2050,1,1);
                                }
                                db.setAccountEndDate(trialEndDate.toString());
                                db.setSuspended(trialEndDate.before(today));
                            } else {
                                db.setBillingAccountName(row.get("baName"));
                                db.setAccountEndDate(new LocalDate(row.getDate("baEndDate")).toString());
                            }
                        }

                        if (db.getAmOwner()) {
                            db.setHasPendingTransfer(row.getBoolean("pendingTransfer"));
                        }

                        if (db.getAmOwner()) {
                            db.setViewAllAllowed(true);
                            db.setCreateAllowed(true);
                            db.setCreateAllAllowed(true);
                            db.setEditAllowed(true);
                            db.setEditAllAllowed(true);
                            db.setDeleteAllowed(true);
                            db.setDeleteAllAllowed(true);
                            db.setManageUsersAllowed(true);
                            db.setManageAllUsersAllowed(true);
                            db.setDesignAllowed(true);
                            db.setExportAllowed(true);

                        } else if (row.isNull("allowViewAll")) {

                            // when other users see public databases
                            // they will not have a UserPermission record
                            db.setViewAllAllowed(true);
                            db.setCreateAllowed(false);
                            db.setCreateAllAllowed(false);
                            db.setEditAllowed(false);
                            db.setEditAllAllowed(false);
                            db.setDeleteAllowed(false);
                            db.setDeleteAllAllowed(false);
                            db.setManageUsersAllowed(false);
                            db.setManageAllUsersAllowed(false);
                            db.setDesignAllowed(false);
                            db.setExportAllowed(true);

                        } else {
                            db.setViewAllAllowed(row.getBoolean("allowViewAll"));
                            db.setCreateAllowed(row.getBoolean("allowCreate"));
                            db.setCreateAllAllowed(row.getBoolean("allowCreateAll"));
                            db.setEditAllowed(row.getBoolean("allowEdit"));
                            db.setEditAllAllowed(row.getBoolean("allowEditAll"));
                            db.setDeleteAllowed(row.getBoolean("allowDelete"));
                            db.setDeleteAllAllowed(row.getBoolean("allowDeleteAll"));
                            db.setManageUsersAllowed(row.getBoolean("allowManageUsers"));
                            db.setManageAllUsersAllowed(row.getBoolean("allowManageAllUsers"));
                            db.setDesignAllowed(row.getBoolean("allowDesign"));
                            db.setExportAllowed(row.getBoolean("allowExport"));
                        }

                        SchemaFilter schemaFilter = buildFilter(db, row.getString("permissionsModel"));
                        if(db.isDesignAllowed() && !schemaFilter.hasFolderLimitations()) {
                            db.setDatabaseDesignAllowed(true);
                        }

                        db.setFolderLimitation(schemaFilter.hasFolderLimitations());

                        databaseFilters.put(db.getId(), schemaFilter);

                        if (!databaseMap.containsKey(db.getId())) {
                            databaseMap.put(db.getId(), db);
                            databaseList.add(db);
                        }
                    }


                    if (databaseMap.isEmpty()) {
                        promise.resolve(null);
                    } else {
                        Promise.waitAll(
                                joinDatabasePartners(),
                                joinAssignedPartners(),
                                loadProjects(),
                                loadFolders(),
                                loadActivities(),
                                loadLockedPeriods())
                                .then(promise);
                    }
                }


            });
            return promise;
        }

        private SchemaFilter buildFilter(UserDatabaseDTO db, String permissionModel) {
            if(db.getAmOwner()) {
                return UNFILTERED;
            }
            if(Strings.isNullOrEmpty(permissionModel)) {
                return UNFILTERED;
            }
            return new FolderFilter(permissionModel);
        }

        protected Promise<Void> loadFolders() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery.select("name", "folderId", "databaseId")
                    .from("folder")
                    .where("databaseId").in(databaseMap.keySet())
                    .orderBy("sortOrder")
                    .orderBy("name")
                    .execute(tx, new SqlResultCallback() {
                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                            for (SqlResultSetRow row : results.getRows()) {
                                int databaseId = row.getInt("databaseId");
                                SchemaFilter filter = databaseFilters.get(databaseId);

                                FolderDTO folder = new FolderDTO();
                                folder.setId(row.getInt("folderId"));
                                folder.setName(row.getString("name"));
                                folder.setDatabaseId(databaseId);

                                if(filter.isVisible(folder)) {
                                    UserDatabaseDTO database = databaseMap.get(databaseId);
                                    database.getFolders().add(folder);
                                }
                                folders.put(folder.getId(), folder);

                            }
                            promise.resolve(null);
                        }
                    });
            return promise;
        }

        protected Promise<Void> loadProjects() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery.select("name", "projectId", "description", "databaseId")
                    .from("project")
                    .where("databaseId").in(databaseMap.keySet())
                    .where("dateDeleted").isNull()
                    .execute(tx, new SqlResultCallback() {
                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                            for (SqlResultSetRow row : results.getRows()) {
                                ProjectDTO project = new ProjectDTO();
                                project.setName(row.getString("name"));
                                project.setId(row.getInt("projectId"));
                                project.setDescription(row.getString("description"));

                                int databaseId = row.getInt("databaseId");
                                UserDatabaseDTO database = databaseMap.get(databaseId);
                                database.getProjects().add(project);
                                project.setUserDatabase(database);
                                projects.put(project.getId(), project);
                            }
                            promise.resolve(null);
                        }
                    });
            return promise;
        }

        protected Promise<Void> loadLockedPeriods() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery.select("fromDate",
                    "toDate",
                    "enabled",
                    "name",
                    "lockedPeriodId",
                    "databaseId",
                    "activityId",
                    "folderId",
                    "projectId")
                    .from("lockedperiod")
                    .where("databaseId").in(databaseMap.keySet())
                    .execute(tx, new SqlResultCallback() {

                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                            for (SqlResultSetRow row : results.getRows()) {
                                LockedPeriodDTO lockedPeriod = new LockedPeriodDTO();

                                lockedPeriod.setFromDate(row.getDate("fromDate"));
                                lockedPeriod.setToDate(row.getDate("toDate"));
                                lockedPeriod.setEnabled(row.getBoolean("enabled"));
                                lockedPeriod.setName(row.getString("name"));
                                lockedPeriod.setId(row.getInt("lockedPeriodId"));

                                if (!row.isNull("activityId")) {
                                    Integer activityId = row.getInt("activityId");
                                    ActivityDTO activity = activities.get(activityId);
                                    if (activity != null) { // activities can be
                                        // deleted...
                                        activity.getLockedPeriods().add(lockedPeriod);
                                        lockedPeriod.setParent(activity);
                                    }
                                } else if (!row.isNull("projectId")) {
                                    Integer projectId = row.getInt("projectId");
                                    ProjectDTO project = projects.get(projectId);
                                    if (project != null) {
                                        project.getLockedPeriods().add(lockedPeriod);
                                        lockedPeriod.setParent(project);
                                    }
                                } else if(!row.isNull("folderId")) {
                                    Integer folderId = row.getInt("folderId");
                                    FolderDTO folder = folders.get(folderId);
                                    if(folder != null) {
                                        folder.getLockedPeriods().add(lockedPeriod);
                                        lockedPeriod.setParent(folder);
                                    }
                                } else {
                                    Integer databaseId = row.getInt("databaseId");
                                    UserDatabaseDTO database = databaseMap.get(databaseId);
                                    if (database != null) { // databases can be
                                        // deleted
                                        database.getLockedPeriods().add(lockedPeriod);
                                        lockedPeriod.setParent(database);
                                    }
                                }
                            }
                            promise.resolve(null);
                        }
                    });
            return promise;
        }

        private Promise<Void> joinDatabasePartners() {
            SqlQuery query = SqlQuery.select("d.databaseId", "d.partnerId", "p.name", "p.fullName")
                    .from(Tables.PARTNER_IN_DATABASE, "d")
                    .leftJoin(Tables.PARTNER, "p")
                    .on("d.PartnerId = p.PartnerId")
                    .orderBy("p.name");

            // Only allow results that are visible to this user if we are on the
            // server,
            // otherwise permissions have already been taken into account during
            // synchronization
            if (context.isRemote()) {
                query.where("d.databaseId").in(databaseMap.keySet());
            }

            return execute(query, new RowHandler() {

                @Override
                public void handleRow(SqlResultSetRow row) {

                    int partnerId = row.getInt("partnerId");
                    PartnerDTO partner = partners.computeIfAbsent(partnerId, id -> {
                        PartnerDTO newPartner = new PartnerDTO();
                        newPartner.setId(id);
                        newPartner.setName(row.getString("name"));
                        newPartner.setFullName(row.getString("fullName"));
                        return newPartner;
                    });

                    UserDatabaseDTO db = databaseMap.get(row.getInt("databaseId"));
                    if (db != null) { // databases can be deleted
                        db.addDatabasePartner(partner);
                    }
                }
            });
        }

        private Promise<Void> joinAssignedPartners() {
            SqlQuery query = SqlQuery.select("g.partnerId", "up.databaseId")
                    .from(Tables.GROUP_ASSIGNMENT, "g")
                    .leftJoin(Tables.USER_PERMISSION, "up")
                    .on("up.UserPermissionId = g.UserPermissionId");

            // Only allow results that are visible to this user if we are on the
            // server,
            // otherwise permissions have already been taken into account during
            // synchronization
            if (context.isRemote()) {
                query.where("up.databaseId").in(databaseMap.keySet())
                        .where("up.UserId").equalTo(context.getUser().getId());
            }

            return execute(query, new RowHandler() {

                @Override
                public void handleRow(SqlResultSetRow row) {

                    int assignedPartnerId = row.getInt("partnerId");
                    PartnerDTO assignedPartner = partners.get(assignedPartnerId);
                    if (assignedPartner == null) {
                        // Partner should have been extracted earlier. This group has been removed from database.
                        return;
                    }

                    UserDatabaseDTO db = databaseMap.get(row.getInt("databaseId"));
                    if (db != null) { // databases can be deleted
                       db.addAssignedPartner(assignedPartner);
                    }
                }
            });
        }

        public Promise<Void> loadActivities() {
            SqlQuery query = SqlQuery.select("activityId",
                    "name",
                    "category",
                    "locationTypeId",
                    "reportingFrequency",
                    "databaseId",
                    "folderId",
                    "classicView",
                    "published").from("activity").orderBy("SortOrder");

            if (context.isRemote()) {
                query.where("DateDeleted IS NULL");
                query.where("DatabaseId").in(databaseMap.keySet());
            }

            return execute(query, new RowHandler() {

                @Override
                public void handleRow(SqlResultSetRow row) {

                    int databaseId = row.getInt("databaseId");

                    ActivityDTO activity = new ActivityDTO();
                    activity.setId(row.getInt("activityId"));
                    activity.setName(row.getString("name"));
                    activity.setCategory(row.getString("category"));
                    activity.setReportingFrequency(row.getInt("reportingFrequency"));
                    activity.setPublished(row.getInt("published"));
                    activity.setClassicView(row.getBoolean("classicView"));

                    if(!row.isNull("folderId")) {
                        int folderId = row.getInt("folderId");
                        FolderDTO folder = folders.get(folderId);
                        if(folder != null) {
                            activity.setFolder(folder);
                            folder.getActivities().add(activity);
                        }
                    }

                    SchemaFilter filter = databaseFilters.get(databaseId);
                    if(filter.isVisible(activity)) {

                        UserDatabaseDTO database = databaseMap.get(databaseId);
                        activity.setDatabase(database);
                        activity.setPartnerRange(database.getAllowablePartners());
                        database.getActivities().add(activity);

                        int locationTypeId = row.getInt("locationTypeId");
                        LocationTypeDTO locationType = locationTypes.get(locationTypeId);

                        if (locationType == null) {
                            throw new IllegalStateException("No location type for " + locationTypeId);
                        }
                        activity.setLocationType(locationType);
                        activity.set("locationTypeId", locationType.getId());


                        activities.put(activity.getId(), activity);
                    }
                }
            });
        }

        private Promise<Void> execute(SqlQuery query, final RowHandler rowHandler) {
            final Promise<Void> promise = new Promise<>();
            query.execute(tx, new SqlResultCallback() {
                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                    rowHandler.onSuccess(tx, results);
                    promise.resolve(null);
                }
            });
            return promise;
        }

        public void build(ExecutionContext context, final AsyncCallback<SchemaDTO> callback) {
            this.context = context;
            this.tx = context.getTransaction();

            List<Promise<Void>> tasks = Lists.newArrayList();

            tasks.add(loadCountries());
            tasks.add(loadAdminLevels());
            tasks.add(loadLocationTypes());

            tasks.add(loadDatabases());

            SchemaDTO schemaDTO = new SchemaDTO();
            schemaDTO.setCountries(countryList);
            schemaDTO.setDatabases(databaseList);

            Promise.waitAll(tasks)
                    .then(Functions.constant(schemaDTO))
                    .then(callback);
        }
    }
}
