package org.activityinfo.store.mysql.collections;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.service.store.ResourceNotFound;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.ResourceFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.metadata.CountryStructure;
import org.activityinfo.store.mysql.update.SqlInsert;
import org.activityinfo.store.mysql.update.SqlUpdate;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;


public class LocationCollection implements ResourceCollection {


    public static final String TABLE_NAME = "location";

    private QueryExecutor executor;
    private final int locationTypeId;
    private final TableMapping mapping;
    private final CountryStructure country;
    private long version;
    private final ResourceId nameFieldId;
    private final ResourceId axeFieldId;
    private final ResourceId adminFieldId;
    private final ResourceId pointFieldId;

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

        nameFieldId = CuidAdapter.field(formClassId, CuidAdapter.NAME_FIELD);
        FormField labelField = new FormField(nameFieldId);
        labelField.setCode("label");
        labelField.setLabel(I18N.CONSTANTS.name());
        labelField.setRequired(true);
        labelField.setType(TextType.INSTANCE);

        axeFieldId = CuidAdapter.field(formClassId, CuidAdapter.AXE_FIELD);
        FormField axeField = new FormField(axeFieldId);
        axeField.setCode("axe");
        axeField.setLabel(I18N.CONSTANTS.axe());
        axeField.setRequired(false);
        axeField.setType(TextType.INSTANCE);

        adminFieldId = CuidAdapter.field(formClassId, CuidAdapter.ADMIN_FIELD);
        FormField adminField = new FormField(adminFieldId);
        adminField.setLabel(I18N.CONSTANTS.adminEntities());
        adminField.setCode("admin");
        adminField.setType(new ReferenceType()
                .setCardinality(Cardinality.MULTIPLE)
                .setRange(country.getAdminLevelFormClassIds()));

        pointFieldId = CuidAdapter.field(formClassId, CuidAdapter.GEOMETRY_FIELD);
        FormField pointField = new FormField(pointFieldId);
        pointField.setCode("point");
        pointField.setLabel(I18N.CONSTANTS.geographicCoordinatesFieldLabel());
        pointField.setRequired(false);
        pointField.setType(GeoPointType.INSTANCE);

//        FormField visible = new FormField(CuidAdapter.field(formClassId, CuidAdapter));
//        visible.setCode("visible");
//        visible.setLabel(I18N.CONSTANTS.visible());
//        visible.setRequired(true);
//        visible.setType(BooleanType.INSTANCE);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, TABLE_NAME);
        mapping.setBaseFilter("base.locationTypeId=" + locationTypeId);
        mapping.setPrimaryKeyMapping(CuidAdapter.LOCATION_DOMAIN, "locationId");
        mapping.setFormLabel(name);
        mapping.setOwnerId(CuidAdapter.countryId(countryId));
        mapping.addTextField(labelField, "name");
        mapping.addTextField(axeField, "axe");
        mapping.addUnmappedField(adminField);
        mapping.addGeoPoint(pointField);

//        if(BETA.ENABLE_BOOLEAN_FIELDS) {
//            mapping.add(new FieldMapping(visible, "workflowStatusId", new FieldValueConverter() {
//                @Override
//                public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
//                    String statusId = rs.getString(index); // either "rejected" or "validatated"
//                    return BooleanFieldValue.valueOf("validated".equals(statusId));
//                }
//
//                @Override
//                public Collection<?> toParameters(FieldValue value) {
//                    return Collections.singletonList(value == BooleanFieldValue.TRUE ? "validated" : "rejected");
//                }
//            }));
//        }
        this.mapping = mapping.build();

    }

    @Override
    public CollectionPermissions getPermissions(int userId) {
        return CollectionPermissions.readonly();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        ResourceFetcher fetcher = new ResourceFetcher(this);
        return fetcher.get(resourceId);
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    private long incrementVersion() {
        long newVersion = version + 1;

        SqlUpdate update = SqlUpdate.update("locationtype");
        update.where("locationTypeId", locationTypeId);
        update.set("version", newVersion);
        update.execute(executor);

        return newVersion;
    }

    @Override
    public void add(ResourceUpdate update) {
        long newVersion = incrementVersion();

        int locationId = CuidAdapter.getLegacyIdFromCuid(update.getResourceId());

        SqlInsert insert = SqlInsert.insertInto("location");
        insert.value("locationTypeId", locationTypeId);
        insert.value("locationId", locationId);
        insert.value("timeEdited", System.currentTimeMillis());
        insert.value("version", newVersion);
        insert.value("name", getName(update), 50);
        insert.value("axe", getAxe(update), 50);

        GeoPoint point = getPoint(update);
        if (point != null) {
            insert.value("x", point.getLongitude());
            insert.value("y", point.getLatitude());
        }
        insert.execute(executor);

        Set<Integer> adminEntities = fetchParents(getAdminEntities(update));

        insertAdminLinks(locationId, adminEntities);

    }


    @Override
    public void update(ResourceUpdate update) {
        long newVersion = incrementVersion();

        int locationId = CuidAdapter.getLegacyIdFromCuid(update.getResourceId());
        
        SqlUpdate sql = SqlUpdate.update("location");
        sql.where("locationId", locationId);
        sql.set("timeEdited", System.currentTimeMillis());
        sql.set("version", newVersion);

        if(update.isDeleted()) {
            sql.set("workflowStatusId", "rejected");

        } else {

            sql.set("name", getName(update), 50);
            sql.set("axe", getAxe(update), 50);

            GeoPoint point = getPoint(update);
            if (point != null) {
                sql.set("x", point.getLongitude());
                sql.set("y", point.getLatitude());
            } else {
                sql.set("x", null);
                sql.set("y", null);
            }
        }
        sql.execute(executor);

        if(!update.isDeleted()) {
            Set<Integer> adminEntities = fetchParents(getAdminEntities(update));

            updateAdminLinks(locationId, adminEntities);
        }
    }

    private Set<Integer> fetchParents(Set<Integer> adminEntities) {
        // Complete list of admin entities to which this location belongs
        Set<Integer> all = Sets.newHashSet();

        // AdminEntities to which this admin entity belongs, but we 
        // need to fetch their parents
        Set<Integer> needParents = Sets.newHashSet(adminEntities);

        while(!needParents.isEmpty()) {

            String sql = "SELECT adminEntityParentId FROM adminentity WHERE adminentityid " +
                    " IN (" + Joiner.on(", ").join(needParents) + ")";

            try(ResultSet rs = executor.query(sql)) {

                all.addAll(needParents);
                needParents.clear();

                while(rs.next()) {
                    int parentId = rs.getInt(1);
                    if(!rs.wasNull()) {
                        needParents.add(parentId);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not fetch admin parents", e);
            }
        }
        return all;
    }

    private void insertAdminLinks(int locationId, Set<Integer> adminEntities) {
        if(!adminEntities.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO locationadminlink (locationId, adminEntityId) ");
            sql.append("VALUES ");

            boolean needsComma = false;
            for (Integer adminEntity : adminEntities) {
                if (needsComma) {
                    sql.append(", ");
                }
                sql.append("(").append(locationId).append(", ").append(adminEntity).append(")");
                needsComma = true;
            }

            System.out.println(sql.toString());

            executor.update(sql.toString(), Collections.emptyList());
        }
    }

    private void updateAdminLinks(int locationId, Set<Integer> adminEntities) {
        executor.update("DELETE FROM locationadminlink WHERE locationId = ?",
                Collections.singletonList(locationId));

        insertAdminLinks(locationId, adminEntities);
    }

    private String getName(ResourceUpdate update) {
        FieldValue fieldValue = update.getChangedFieldValues().get(nameFieldId);
        if (fieldValue == null) {
            return null;
        } else {
            return ((TextValue) fieldValue).asString();
        }
    }

    private String getAxe(ResourceUpdate update) {
        FieldValue fieldValue = update.getChangedFieldValues().get(axeFieldId);
        if (fieldValue == null) {
            return null;
        } else {
            return ((TextValue) fieldValue).asString();
        }
    }

    private GeoPoint getPoint(ResourceUpdate update) {
        return (GeoPoint) update.getChangedFieldValues().get(pointFieldId);
    }

    private Set<Integer> getAdminEntities(ResourceUpdate update) {
        Set<Integer> set = Sets.newHashSet();
        ReferenceValue value = (ReferenceValue) update.getChangedFieldValues().get(adminFieldId);
        if(value != null) {
            for (ResourceId resourceId : value.getResourceIds()) {
                set.add(CuidAdapter.getLegacyIdFromCuid(resourceId));
            }
        }
        return set;
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
