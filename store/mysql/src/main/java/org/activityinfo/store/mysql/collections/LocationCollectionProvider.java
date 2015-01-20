package org.activityinfo.store.mysql.collections;

import com.google.common.base.Preconditions;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.MappingProvider;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;


public class LocationCollectionProvider implements MappingProvider {

    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4BFD0970004");
    public static final ResourceId AXE_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C1F2E60006");
    public static final ResourceId ADMIN_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C59E540009");
    public static final ResourceId POINT_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C5D7C6000A");
    public static final String TABLE_NAME = "location";

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formClassId) throws SQLException {

        int locationTypeId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        int countryId;

        String sql = "SELECT * FROM locationtype LT WHERE LocationTypeId = " + locationTypeId;

        try (ResultSet rs = executor.query(sql)) {

            if (!rs.next()) {
                throw new ResourceNotFound(formClassId);
            }

            countryId = rs.getInt("countryId");
            Preconditions.checkState(!rs.wasNull());

            FormField labelField = new FormField(NAME_FIELD_ID);
            labelField.setCode("label");
            labelField.setLabel(I18N.CONSTANTS.name());
            labelField.setRequired(true);
            labelField.setType(TextType.INSTANCE);

            FormField axeField = new FormField(AXE_FIELD_ID);
            axeField.setCode("axe");
            axeField.setLabel(I18N.CONSTANTS.axe());
            axeField.setRequired(false);
            axeField.setType(TextType.INSTANCE);
//
//            FormField adminField = new FormField(ADMIN_FIELD_ID);
//            adminField.setLabel(I18N.CONSTANTS.adminEntities());
//            adminField.setCode("admin");
//            adminField.setType(queryAdminType(executor, countryId));

            FormField pointField = new FormField(POINT_FIELD_ID);
            pointField.setCode("point");
            pointField.setLabel(I18N.CONSTANTS.geographicCoordinatesFieldLabel());
            pointField.setRequired(false);
            pointField.setType(GeoAreaType.INSTANCE);

            TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, TABLE_NAME);
            mapping.setBaseFilter("base.locationTypeId=" + locationTypeId);
            mapping.setPrimaryKeyMapping(CuidAdapter.LOCATION_DOMAIN, "locationId");
            mapping.setFormLabel(rs.getString("name"));
            mapping.setOwnerId(CuidAdapter.countryId(countryId));
            mapping.addTextField(labelField, "name");
            mapping.addTextField(axeField, "axe");
            mapping.addGeoPoint(pointField);
            return mapping.build();
        }
    }
//
//    private FieldType queryAdminType(QueryExecutor executor, int countryId) throws SQLException {
//
//        Set<ResourceId> range = Sets.newHashSet();
//
//        String sql =
//                "SELECT * " +
//                "FROM adminlevel L " +
//                "WHERE boundadminlevelid IS NULL AND " +
//                      "name <> 'Nationwide' AND " +
//                      "countryid = " + countryId;
//
//        try (ResultSet rs = executor.query(sql)) {
//            while(rs.next()) {
//                int adminLevelId = rs.getInt("adminLevelId");
//                range.add(CuidAdapter.resourceId(CuidAdapter.ADMIN_LEVEL_DOMAIN, rs.geti))
//
//            }
//        }
//        return null;
//    }


}
