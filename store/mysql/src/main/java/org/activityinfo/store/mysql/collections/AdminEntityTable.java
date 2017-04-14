package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.activityinfo.model.legacy.CuidAdapter.ADMIN_ENTITY_DOMAIN;

/**
 * Provides access to collections of administrative entities
 */
public class AdminEntityTable implements SimpleTable {
    
    public static final String ADMIN_ENTITY_TABLE = "adminentity";

    /**
     * Admin levels change very infrequently, and then only the name can change, so 
     * we can safely retain unconditionally for a reasonable period of time.
     */
    private static final Cache<Integer, AdminLevel> LEVEL_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(10)
            .expireAfterWrite(10, TimeUnit.HOURS)
            .build();

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {

        int levelId = CuidAdapter.getLegacyIdFromCuid(formId);
        AdminLevel level = LEVEL_CACHE.getIfPresent(levelId);
        if(level == null) {
            level = AdminLevel.fetch(executor, levelId);
            LEVEL_CACHE.put(levelId, level);
        }

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
        bounds.setRequired(true);
        bounds.setType(GeoAreaType.INSTANCE);
        
        FormField parent = null;
        if(level.hasParent()) {
            parent = new FormField(CuidAdapter.field(formId, CuidAdapter.PARTNER_FIELD));
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

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor queryExecutor, ResourceId id) throws SQLException {
        if(id.getDomain() == ADMIN_ENTITY_DOMAIN) {
            try(ResultSet rs = queryExecutor.query(String.format("SELECT adminLevelId FROM adminentity WHERE adminEntityId=%d",
                    CuidAdapter.getLegacyIdFromCuid(id)))) {
                if (rs.next()) {
                    int adminLevelId = rs.getInt(1);
                    return Optional.of(CuidAdapter.adminLevelFormClass(adminLevelId));
                }
            }
        }
        return Optional.absent();
    }

    public static void clearCache() {
        LEVEL_CACHE.invalidateAll();
    }
}
