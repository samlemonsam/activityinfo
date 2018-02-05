package org.activityinfo.store.mysql.collections;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.mysql.GeodbFolder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.RecordFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.metadata.CountryStructure;
import org.activityinfo.store.mysql.metadata.PermissionsCache;
import org.activityinfo.store.mysql.metadata.UserPermission;
import org.activityinfo.store.mysql.update.SqlInsert;
import org.activityinfo.store.mysql.update.SqlUpdate;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormNotFoundException;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class LocationFormStorage implements FormStorage {


    public static final String TABLE_NAME = "location";

    private QueryExecutor executor;
    private final int locationTypeId;
    private Integer databaseId;
    private boolean openWorkflow;
    private PermissionsCache permissionsCache;
    private final TableMapping mapping;
    private final CountryStructure country;
    private long version;
    private final ResourceId nameFieldId;
    private final ResourceId axeFieldId;
    private final ResourceId adminFieldId;
    private final ResourceId pointFieldId;

    public LocationFormStorage(QueryExecutor executor, ResourceId formClassId, PermissionsCache permissionsCache) throws SQLException {

        this.executor = executor;

        locationTypeId = CuidAdapter.getLegacyIdFromCuid(formClassId);
        this.permissionsCache = permissionsCache;
        int countryId;
        String name;

        String sql = "SELECT * FROM locationtype LT WHERE LocationTypeId = " + locationTypeId;

        try (ResultSet rs = executor.query(sql)) {
            if (!rs.next()) {
                throw new FormNotFoundException(formClassId);
            }
            countryId = rs.getInt("countryId");
            Preconditions.checkState(!rs.wasNull());

            openWorkflow = "open".equals(rs.getString("workflowId"));

            databaseId = rs.getInt("databaseId");
            if(rs.wasNull()) {
                databaseId = null;
            }
            name = rs.getString("name");
            version = rs.getLong("version");
        }

        this.country = CountryStructure.query(executor, countryId);

        nameFieldId = CuidAdapter.field(formClassId, CuidAdapter.NAME_FIELD);
        FormField labelField = new FormField(nameFieldId);
        labelField.setCode("name");
        labelField.setKey(true);
        labelField.setLabel(I18N.CONSTANTS.name());
        labelField.addSuperProperty(ResourceId.valueOf("label"));
        labelField.setRequired(true);
        labelField.setType(TextType.SIMPLE);

        axeFieldId = CuidAdapter.field(formClassId, CuidAdapter.AXE_FIELD);
        FormField axeField = new FormField(axeFieldId);
        axeField.setCode("axe");
        axeField.setLabel(I18N.CONSTANTS.alternateName());
        axeField.setRequired(false);
        axeField.setType(TextType.SIMPLE);

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

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formClassId, TABLE_NAME);
        mapping.setBaseFilter("base.locationTypeId=" + locationTypeId + " AND workflowStatusId='validated'");
        mapping.setPrimaryKeyMapping(CuidAdapter.LOCATION_DOMAIN, "locationId");
        mapping.setFormLabel(name);
        mapping.setDatabaseId(GeodbFolder.GEODB_ID);
        mapping.addTextField(labelField, "name");
        mapping.addTextField(axeField, "axe");
        mapping.addUnmappedField(adminField);
        mapping.addGeoPoint(pointField);

        // TODO: The schema of a location type can actually change if we add additional
        // admin levels to a country.
        mapping.setSchemaVersion(1L);

        this.mapping = mapping.build();

    }

    @Override
    public FormPermissions getPermissions(int userId) {
        if(openWorkflow) {
            return FormPermissions.readWrite();
        }
        if(databaseId != null) {
            UserPermission permission = permissionsCache.getPermission(userId, databaseId);
            if (permission.isDesign()) {
                return FormPermissions.builder().allowEdit().build();
            }
        }
        return FormPermissions.readonly();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        RecordFetcher fetcher = new RecordFetcher(this);
        return fetcher.get(resourceId);
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
        throw new UnsupportedOperationException();
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
    public void add(TypedRecordUpdate update) {
        long newVersion = incrementVersion();

        int locationId = CuidAdapter.getLegacyIdFromCuid(update.getRecordId());

        SqlInsert insert = SqlInsert.insertInto("location");
        insert.value("locationTypeId", locationTypeId);
        insert.value("locationId", locationId);
        insert.value("timeEdited", System.currentTimeMillis());
        insert.value("version", newVersion);
        insert.value("name", getName(update), 50);
        insert.value("axe", getAxe(update), 50);

        GeoPoint point = (GeoPoint) update.getChangedFieldValues().get(pointFieldId);
        if (point != null) {
            insert.value("x", point.getLongitude());
            insert.value("y", point.getLatitude());
        }
        insert.execute(executor);

        Set<Integer> adminEntities = fetchParents(getAdminEntities(update));

        insertAdminLinks(locationId, adminEntities);

    }


    @Override
    public void update(TypedRecordUpdate update) {
        long newVersion = incrementVersion();

        int locationId = CuidAdapter.getLegacyIdFromCuid(update.getRecordId());
        
        SqlUpdate sql = SqlUpdate.update("location");
        sql.where("locationId", locationId);
        sql.set("timeEdited", System.currentTimeMillis());
        sql.set("version", newVersion);

        if(update.isDeleted()) {
            sql.set("workflowStatusId", "rejected");

        } else {

            sql.set("name", getName(update), 50);
            sql.set("axe", getAxe(update), 50);

            if(update.getChangedFieldValues().containsKey(pointFieldId)) {
                GeoPoint point = (GeoPoint) update.getChangedFieldValues().get(pointFieldId);
                if(point == null) {
                    sql.set("x", null);
                    sql.set("y", null);
                } else {
                    sql.set("x", point.getLongitude());
                    sql.set("y", point.getLatitude());
                }
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

    private String getName(TypedRecordUpdate update) {
        FieldValue fieldValue = update.getChangedFieldValues().get(nameFieldId);
        if (fieldValue == null) {
            return null;
        } else {
            return ((TextValue) fieldValue).asString();
        }
    }

    private String getAxe(TypedRecordUpdate update) {
        FieldValue fieldValue = update.getChangedFieldValues().get(axeFieldId);
        if (fieldValue == null) {
            return null;
        } else {
            return ((TextValue) fieldValue).asString();
        }
    }

    private Set<Integer> getAdminEntities(TypedRecordUpdate update) {
        Set<Integer> set = Sets.newHashSet();
        ReferenceValue value = (ReferenceValue) update.getChangedFieldValues().get(adminFieldId);
        if(value != null) {
            for (RecordRef ref : value.getReferences()) {
                set.add(CuidAdapter.getLegacyIdFromCuid(ref.getRecordId()));
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

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }

}
