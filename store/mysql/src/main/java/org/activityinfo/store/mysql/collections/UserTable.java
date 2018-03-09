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
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;


public class UserTable implements SimpleTable {
    
    public static final ResourceId USERDB_ID = ResourceId.valueOf("userdb");

    private static final String TABLE_NAME = "userlogin";

    public static final ResourceId FORM_CLASS_ID = ResourceId.valueOf("FF8081814AE2C808014AE2DEE2270009");
    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("FF8081814AE2C808014AE2DEFDAB000A");

    private final TableMapping mapping;

    public UserTable() {

        FormField nameField = new FormField(NAME_FIELD_ID);
        nameField.setLabel("Name");
        nameField.setType(TextType.SIMPLE);
        nameField.setRequired(true);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(FORM_CLASS_ID, TABLE_NAME);
        mapping.setFormLabel("Users");
        mapping.setDatabaseId(USERDB_ID);
        
        mapping.setPrimaryKeyMapping(CuidAdapter.USER_DOMAIN, "UserId");
        mapping.addTextField(nameField, "name");
        this.mapping = mapping.build();
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.equals(FORM_CLASS_ID);
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) {
        return mapping;
    }

}
