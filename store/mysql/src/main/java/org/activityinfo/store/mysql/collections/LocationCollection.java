package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.FieldMapping;
import org.activityinfo.store.mysql.mapping.FieldValueConverter;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;


public class LocationCollection implements ResourceCollection {

    public static final ResourceId NAME_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4BFD0970004");
    public static final ResourceId AXE_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C1F2E60006");
    public static final ResourceId ADMIN_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C59E540009");
    public static final ResourceId POINT_FIELD_ID = ResourceId.valueOf("FF8081814AE3CC9B014AE4C5D7C6000A");
    public static final ResourceId VISIBLE_FIELD_ID = ResourceId.valueOf("FF808181525E888601525E88867F0000");

    public static final String TABLE_NAME = "location";
    
    private QueryExecutor executor;
    private final int locationTypeId;
    private final TableMapping mapping;
    private final CountryStructure country;
    private long version;

    public LocationCollection(QueryExecutor executor, ResourceId formClassId) throws SQLException {

        this.executor = executor;

        locationTypeId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        int countryId;
        String name;
        
        String sql = "SELECT * FROM locationtype LT WHERE LocationTypeId = " + locationTypeId;

        try (ResultSet rs = executor.query(sql)) {
            if (!rs.next()) {
                throw new ResourceNotFound(formClassId);
            }
            countryId = rs.getInt("countryId");
            Preconditions.checkState(!rs.wasNull());

            name = rs.getString("name");
            version = rs.getLong("version");
        }

        this.country = CountryStructure.query(executor, countryId);

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

        FormField adminField = new FormField(ADMIN_FIELD_ID);
        adminField.setLabel(I18N.CONSTANTS.adminEntities());
        adminField.setCode("admin");
        adminField.setType(new ReferenceType()
                .setCardinality(Cardinality.MULTIPLE)
                .setRange(country.getAdminLevelFormClassIds()));

        FormField pointField = new FormField(POINT_FIELD_ID);
        pointField.setCode("point");
        pointField.setLabel(I18N.CONSTANTS.geographicCoordinatesFieldLabel());
        pointField.setRequired(false);
        pointField.setType(GeoAreaType.INSTANCE);
        
        FormField visible = new FormField(VISIBLE_FIELD_ID);
        visible.setCode("visible");
        visible.setLabel(I18N.CONSTANTS.visible());
        visible.setRequired(true);
        visible.setType(BooleanType.INSTANCE);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, TABLE_NAME);
        mapping.setBaseFilter("base.locationTypeId=" + locationTypeId);
        mapping.setPrimaryKeyMapping(CuidAdapter.LOCATION_DOMAIN, "locationId");
        mapping.setFormLabel(name);
        mapping.setOwnerId(CuidAdapter.countryId(countryId));
        mapping.addTextField(labelField, "name");
        mapping.addTextField(axeField, "axe");
        mapping.addUnmappedField(adminField);
        mapping.addGeoPoint(pointField);
        
        if(BETA.ENABLE_BOOLEAN_FIELDS) {
            mapping.add(new FieldMapping(visible, "workflowStatusId", new FieldValueConverter() {
                @Override
                public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
                    String statusId = rs.getString(index); // either "rejected" or "validatated"
                    return BooleanFieldValue.valueOf("validated".equals(statusId));
                }

                @Override
                public Collection<?> toParameters(FieldValue value) {
                    return Collections.singletonList(value == BooleanFieldValue.TRUE ? "validated" : "rejected");
                }
            }));
        }
        this.mapping = mapping.build();

    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        return CollectionPermissions.readonly();
    }

    @Override
    public Optional<Resource> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public void add(ResourceUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(ResourceUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new LocationQueryBuilder(executor, mapping, country);
    }

    @Override
    public long cacheVersion() {
        return version;
    }

}
