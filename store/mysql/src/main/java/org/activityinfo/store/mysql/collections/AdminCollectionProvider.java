package org.activityinfo.store.mysql.collections;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.ADMIN_ENTITY_DOMAIN;

/**
 * Provides access to collections of administrative entities
 */
public class AdminCollectionProvider implements MappingProvider {


    public static final String ADMIN_ENTITY_TABLE = "adminentity";

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) throws SQLException {

        // The shape of the AdminEntity FormClass is determined in part by the parameters
        // set in the adminlevel table

        try(ResultSet rs = executor.query(
                "SELECT " +
                        "L.Name, " +
                        "L.parentId ParentId, " +
                        "P.Name ParentLevelName, " +
                        "L.CountryId " +
                        "FROM adminlevel L " +
                        "LEFT JOIN adminlevel P ON (L.parentId = P.AdminLevelId) " +
                        "WHERE L.AdminLevelId = " + CuidAdapter.getLegacyIdFromCuid(formClassId))) {

            if(!rs.next()) {
                throw new ResourceNotFound(formClassId);
            }

            FormField label = new FormField(CuidAdapter.field(formClassId, CuidAdapter.NAME_FIELD));
            label.setLabel(I18N.CONSTANTS.name());
            label.setCode("name");
            label.setRequired(true);
            label.setType(TextType.INSTANCE);

            FormField code = new FormField(CuidAdapter.field(formClassId, CuidAdapter.CODE_FIELD));
            code.setCode("code");
            code.setLabel(I18N.CONSTANTS.codeFieldLabel());
            code.setRequired(true);
            code.setType(TextType.INSTANCE);            
            
            FormField parent = null;
            int parentId = rs.getInt("parentId");
            if(!rs.wasNull()) {
                parent = new FormField(CuidAdapter.field(formClassId, CuidAdapter.PARTNER_FIELD));
                parent.setCode("parent");
                parent.setLabel(rs.getString("ParentLevelName"));
                parent.setRequired(true);
                parent.setType(ReferenceType.single(CuidAdapter.adminLevelFormClass(parentId)));
            }

            // TODO: geometry
            TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, ADMIN_ENTITY_TABLE);
            mapping.setOwnerId(ResourceId.ROOT_ID);
            mapping.setPrimaryKeyMapping(CuidAdapter.ADMIN_ENTITY_DOMAIN, "adminEntityId");
            mapping.setBaseFilter("base.AdminLevelId=" + CuidAdapter.getLegacyIdFromCuid(formClassId));
            mapping.setFormLabel(rs.getString("Name"));
            mapping.addTextField(label, "name");
            mapping.addTextField(code, "code");

            if(parent != null) {
                mapping.add(new FieldMapping(parent, "adminEntityParentId", new ForeignKeyMapping(ADMIN_ENTITY_DOMAIN)));
            }
            return mapping.build();
        }
    }
}
