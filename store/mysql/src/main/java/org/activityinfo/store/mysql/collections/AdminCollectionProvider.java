package org.activityinfo.store.mysql.collections;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

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
                        "P.Name ParentLevelName " +
                        "L.CountryId " +
                        "FROM adminlevel L " +
                        "LEFT JOIN adminlevel P ON (L.parentId = P.AdminLevelId) " +
                        "WHERE L.AdminLevelId = " + CuidAdapter.getLegacyIdFromCuid(formClassId))) {

            if(!rs.next()) {
                throw new ResourceNotFound(formClassId);
            }

            FormField label = new FormField(CuidAdapter.field(formClassId, CuidAdapter.NAME_FIELD));
            label.setCode("name");
            label.setLabel(I18N.CONSTANTS.name());
            label.setRequired(true);
            label.setType(TextType.INSTANCE);

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
            mapping.setPrimaryKeyMapping(CuidAdapter.ADMIN_ENTITY_DOMAIN, "adminEntityId");
            mapping.setFormLabel(rs.getString("Name"));
            mapping.addTextField(label, "name");

            if(parent != null) {
                mapping.addReferenceField(parent, CuidAdapter.ADMIN_ENTITY_DOMAIN, "admin");
            }
            return mapping.build();
        }
    }
}
