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

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.ApplicationProperties;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.GeodbFolder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.*;
import org.activityinfo.store.mysql.metadata.AdminLevel;

import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.ADMIN_ENTITY_DOMAIN;

/**
 * Provides access to collections of administrative entities
 */
public class AdminEntityTable implements SimpleTable {
    
    public static final String ADMIN_ENTITY_TABLE = "adminentity";

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN &&
                CuidAdapter.isValidLegacyId(formClassId);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {

        int levelId = CuidAdapter.getLegacyIdFromCuid(formId);
        AdminLevel level = AdminLevel.fetch(executor, levelId);

        FormField label = new FormField(CuidAdapter.field(formId, CuidAdapter.NAME_FIELD));
        label.setLabel(I18N.CONSTANTS.name());
        label.setCode("name");
        label.setKey(true);
        label.setRequired(true);
        label.addSuperProperty(ResourceId.valueOf("label"));
        label.setType(TextType.SIMPLE);

        FormField code = new FormField(CuidAdapter.field(formId, CuidAdapter.CODE_FIELD));
        code.setCode("code");
        code.setLabel(I18N.CONSTANTS.codeFieldLabel());
        code.setRequired(false);
        code.setType(TextType.SIMPLE);
        
        FormField bounds = new FormField(CuidAdapter.field(formId, CuidAdapter.GEOMETRY_FIELD));
        bounds.setCode("boundary");
        bounds.setLabel(I18N.CONSTANTS.geography());
        bounds.setType(GeoAreaType.INSTANCE);
        
        FormField parent = null;
        if(level.hasParent()) {
            parent = new FormField(CuidAdapter.field(formId, CuidAdapter.ADMIN_PARENT_FIELD));
            parent.setCode("parent");
            parent.setLabel(level.getParentName());
            parent.setRequired(true);
            parent.setKey(true);
            parent.setType(ReferenceType.single(CuidAdapter.adminLevelFormClass(level.getParentId())));
            parent.addSuperProperty(ApplicationProperties.PARENT_PROPERTY);
        }

        // TODO: geometry
        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formId, ADMIN_ENTITY_TABLE);
        mapping.setDatabaseId(GeodbFolder.GEODB_ID);
        mapping.setPrimaryKeyMapping(CuidAdapter.ADMIN_ENTITY_DOMAIN, "adminEntityId");
        mapping.setBaseFilter("base.AdminLevelId=" + levelId + " AND base.deleted=0");
        mapping.defaultValueOnInsert("AdminLevelId", levelId);
        mapping.setFormLabel(level.getName());
        mapping.addTextField(label, "name");
        mapping.addTextField(code, "code");
        mapping.addGeoAreaField(bounds);
        mapping.setDeleteMethod(DeleteMethod.SOFT_BY_BOOLEAN);
        mapping.setVersion(level.getVersion());
        
        if(parent != null) {
            mapping.add(new FieldMapping(parent, "adminEntityParentId", new ReferenceConverter(
                    CuidAdapter.adminLevelFormClass(level.getParentId()), ADMIN_ENTITY_DOMAIN)));
        }
        return mapping.build();
    }
}
