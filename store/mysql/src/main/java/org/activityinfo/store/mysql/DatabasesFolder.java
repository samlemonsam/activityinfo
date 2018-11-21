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
package org.activityinfo.store.mysql;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.Json;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.database.UserPermissionModel;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.PermissionsCache;
import org.activityinfo.store.mysql.metadata.UserPermission;
import org.activityinfo.store.spi.FormStorageProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabasesFolder {

    public static final String ROOT_ID = "databases";

    private final QueryExecutor executor;
    private final FormStorageProvider formStorageProvider;
    private final PermissionsCache permissionsCache;

    public DatabasesFolder(QueryExecutor executor, FormStorageProvider formStorageProvider) {
        this.executor = executor;
        this.formStorageProvider = formStorageProvider;
        this.permissionsCache = new PermissionsCache(executor);
    }

    public CatalogEntry getRootEntry() {
        return new CatalogEntry(ROOT_ID, I18N.CONSTANTS.databases(), CatalogEntryType.FOLDER);
    }

    public List<CatalogEntry> getChildren(String parentId, int userId) throws SQLException {
        if (parentId.equals(ROOT_ID)) {
            return queryDatabases(userId);
        } else if (ResourceId.valueOf(parentId).getDomain() == CuidAdapter.DATABASE_DOMAIN) {
            List<CatalogEntry> children = new ArrayList<>();
            children.addAll(queryFolders(ResourceId.valueOf(parentId), userId));
            children.addAll(queryRootForms(ResourceId.valueOf(parentId), userId));
            children.addAll(queryLocationTypes(ResourceId.valueOf(parentId)));
            return children;
        } else if (ResourceId.valueOf(parentId).getDomain() == CuidAdapter.FOLDER_DOMAIN) {
            List<CatalogEntry> children = new ArrayList<>();
            children.addAll(queryFolderForms(ResourceId.valueOf(parentId)));
            return children;
        }
        return Collections.emptyList();
    }

    private List<CatalogEntry> queryDatabases(int userId) throws SQLException {
        List<CatalogEntry> entries = new ArrayList<>();

        try (ResultSet rs = executor.query("select d.name, d.databaseId " +
                "FROM userdatabase d " +
                "WHERE d.dateDeleted is NULL AND (d.ownerUserId = ? OR d.databaseId IN " +
                " (select databaseid from userpermission where userpermission.userId = ? and allowview=1))" +
                "ORDER BY d.name", userId, userId)) {
            while (rs.next()) {
                String label = rs.getString(1);
                String formId = CuidAdapter.databaseId(rs.getInt(2)).asString();
                entries.add(new CatalogEntry(formId, label, CatalogEntryType.FOLDER));
            }
        }
        return entries;
    }

    private List<CatalogEntry> queryFolders(ResourceId databaseId, int userId) throws SQLException {
        List<CatalogEntry> entries = new ArrayList<>();
        List<Integer> folderGrants = queryFolderGrants(CuidAdapter.getLegacyIdFromCuid(databaseId), userId);
        try(ResultSet rs = executor.query("SELECT f.folderId, f.name " +
                "FROM folder f " +
                "WHERE f.databaseId = ? " +
                "ORDER BY f.sortOrder ASC", CuidAdapter.getLegacyIdFromCuid(databaseId))) {
            while (rs.next()) {
                int folderId = rs.getInt(1);
                String label = rs.getString(2);
                if (!hasFolderAccess(folderId, folderGrants)) {
                    continue;
                }
                entries.add(new CatalogEntry(CuidAdapter.folderId(folderId).asString(), label, CatalogEntryType.FOLDER));
            }
        }
        return entries;
    }

    private List<CatalogEntry> queryRootForms(ResourceId databaseId, int userId) throws SQLException {
        Map<ResourceId,CatalogEntry> formEntries = Maps.newHashMap();
        List<Integer> folderGrants = queryFolderGrants(CuidAdapter.getLegacyIdFromCuid(databaseId), userId);
        if (!folderGrants.isEmpty()) {
            // user does not have root database folder access rights
            return Collections.emptyList();
        }
        try(ResultSet rs = executor.query("SELECT " +
                    "ActivityId, " +
                    "name " +
                "FROM activity " +
                "WHERE dateDeleted IS NULL " +
                "AND databaseId = ? " +
                "AND folderId IS NULL", CuidAdapter.getLegacyIdFromCuid(databaseId))) {
            while(rs.next()) {
                formEntries.put(CuidAdapter.activityFormClass(rs.getInt(1)), formEntry(rs));
            }
        }
        checkFormLeafStatus(formEntries);
        return new ArrayList<>(formEntries.values());
    }

    private void checkFormLeafStatus(Map<ResourceId, CatalogEntry> formEntries) {
        Map<ResourceId,FormClass> formClasses = formStorageProvider.getFormClasses(formEntries.keySet());
        formClasses.forEach((formId,formClass) -> {
            if (hasSubForm(formClass)) {
                formEntries.get(formId).setLeaf(false);
            }
        });
    }


    private boolean hasSubForm(FormClass formClass) {
        for (FormField field : formClass.getFields()) {
            if (field.getType() instanceof SubFormReferenceType) {
                return true;
            }
        }
        return false;
    }

    private List<CatalogEntry> queryFolderForms(ResourceId folderId) throws SQLException {
        Map<ResourceId,CatalogEntry> formEntries = Maps.newHashMap();
        try(ResultSet rs = executor.query("SELECT " +
                "ActivityId, " +
                "name " +
            "FROM activity " +
            "WHERE dateDeleted IS NULL " +
            "AND folderId = ?", CuidAdapter.getLegacyIdFromCuid(folderId))) {
            while(rs.next()) {
                formEntries.put(CuidAdapter.activityFormClass(rs.getInt(1)), formEntry(rs));
            }
        }
        checkFormLeafStatus(formEntries);
        return new ArrayList<>(formEntries.values());
    }

    private CatalogEntry formEntry(ResultSet rs) throws SQLException {
        String formId = CuidAdapter.activityFormClass(rs.getInt(1)).asString();
        String label = rs.getString(2);
        return new CatalogEntry(formId, label, CatalogEntryType.FORM);
    }

    private boolean hasFolderAccess(int folderId, List<Integer> folderGrants) {
        if (folderGrants.isEmpty()) {
            // User has ALL folder permission - access granted
            return true;
        } else if (folderId == 0) {
            // Root database folder, requires ALL folder permission - access denied
            return false;
        } else {
            // Check whether user has been granted access to folder
            return folderGrants.contains(folderId);
        }
    }

    private List<Integer> queryFolderGrants(int databaseId, int userId) {
        UserPermission permission = permissionsCache.getPermission(userId, databaseId);
        if (Strings.isNullOrEmpty(permission.getModel())) {
            return Collections.emptyList();
        }
        UserPermissionModel model = UserPermissionModel.fromJson(Json.parse(permission.getModel()));
        return model.getGrants().stream()
                .map(GrantModel::getResourceId)
                .filter(id -> id.getDomain() == CuidAdapter.FOLDER_DOMAIN)
                .map(CuidAdapter::getLegacyIdFromCuid)
                .collect(Collectors.toList());
    }

    private List<CatalogEntry> queryLocationTypes(ResourceId databaseId) throws SQLException {
        List<CatalogEntry> entries = new ArrayList<>();
        try(ResultSet rs = executor.query("SELECT locationTypeId, name FROM locationtype " +
                "WHERE databaseId = ? ", CuidAdapter.getLegacyIdFromCuid(databaseId))) {
            while(rs.next()) {
                String formId = CuidAdapter.locationFormClass(rs.getInt(1)).asString();
                String label = rs.getString(2);
                entries.add(new CatalogEntry(formId, label, CatalogEntryType.FORM));
            }
        }

        return entries;
    }
}
