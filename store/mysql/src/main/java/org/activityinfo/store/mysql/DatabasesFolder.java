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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabasesFolder {

    public static final String ROOT_ID = "databases";

    private ActivityLoader activityLoader;
    private final QueryExecutor executor;

    public DatabasesFolder( QueryExecutor executor) {
        this.executor = executor;
    }

    public CatalogEntry getRootEntry() {
        return new CatalogEntry(ROOT_ID, I18N.CONSTANTS.databases(), CatalogEntryType.FOLDER);
    }

    public List<CatalogEntry> getChildren(String parentId, int userId) throws SQLException {
        if (parentId.equals(ROOT_ID)) {
            return queryDatabases(userId);
        } else if (ResourceId.valueOf(parentId).getDomain() == CuidAdapter.DATABASE_DOMAIN) {
            List<CatalogEntry> children = new ArrayList<>();
            children.addAll(queryForms(ResourceId.valueOf(parentId)));
            children.addAll(queryLocationTypes(ResourceId.valueOf(parentId)));
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


    private List<CatalogEntry> queryForms(ResourceId databaseId) throws SQLException {
        List<CatalogEntry> entries = new ArrayList<>();
        try(ResultSet rs = executor.query("SELECT " +
            "ActivityId, " +
            "name, " +
            "(ActivityId IN (SELECT ActivityId FROM indicator WHERE indicator.type='subform')) subforms " +
            "FROM activity " +
                "WHERE dateDeleted IS NULL AND databaseId = ? ", CuidAdapter.getLegacyIdFromCuid(databaseId))) {
            while(rs.next()) {
                String formId = CuidAdapter.activityFormClass(rs.getInt(1)).asString();
                String label = rs.getString(2);
                boolean hasSubForms = rs.getBoolean(3);
                CatalogEntry entry = new CatalogEntry(formId, label, CatalogEntryType.FORM);
                entry.setLeaf(!hasSubForms);
                entries.add(entry);
            }
        }
        return entries;
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
