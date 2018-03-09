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
package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.DeleteMethod;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.metadata.DatabaseCache;

import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.*;

public class ProjectTable implements SimpleTable {
    
    private final DatabaseCache databaseVersionMap;

    public ProjectTable(DatabaseCache databaseVersionMap) {
        this.databaseVersionMap = databaseVersionMap;
    }

    @Override
    public boolean accept(ResourceId formId) {
        return formId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN &&
                CuidAdapter.isValidLegacyId(formId);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formId);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formId, "partner");
        mapping.setFormLabel("Project");
        mapping.setDatabaseId(CuidAdapter.databaseId(databaseId));
        mapping.setPrimaryKeyMapping(CuidAdapter.PROJECT_DOMAIN, "projectId");
        mapping.setFromClause("project base");
        mapping.setBaseFilter("dateDeleted IS NULL AND databaseId=" + databaseId);
        mapping.setDeleteMethod(DeleteMethod.SOFT_BY_DATE);
        mapping.setVersion(databaseVersionMap.getSchemaVersion(databaseId));
        mapping.setSchemaVersion(1L); // Schema is static
        
        FormField nameField = new FormField(field(formId, NAME_FIELD))
                .setRequired(true)
                .setLabel("Name")
                .setCode("label")
                .setKey(true)
                .setType(TextType.SIMPLE);

        mapping.addTextField(nameField, "name");


        FormField fullNameField = new FormField(field(formId, FULL_NAME_FIELD))
                .setLabel("Description")
                .setRequired(false)
                .setType(TextType.SIMPLE);

        mapping.addTextField(fullNameField, "description");

        return mapping.build();
    }

}
