package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.geo.GeoPoint;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class TableMappingBuilder {
    private String tableName;
    private String baseFilter;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;
    private FormClass formClass;

    private TableMappingBuilder(ResourceId formClassId, String tableName) {
        this.tableName = tableName;
        this.formClass = new FormClass(formClassId);
    }

    public static TableMappingBuilder newMapping(ResourceId formClassId, String tableName) {
        return new TableMappingBuilder(formClassId, tableName);
    }

    public void setPrimaryKeyMapping(char domain, String columnName) {
        this.primaryKeyMapping = new PrimaryKeyMapping(domain, columnName);
    }

    public void setFormLabel(String name) {
        this.formClass.setLabel(name);
    }

    public void setOwnerId(ResourceId rootId) {
        formClass.setOwnerId(rootId);
    }
    
    public void setBaseFilter(String baseFilter) {
        this.baseFilter = baseFilter;
    }

    public void add(FieldMapping fieldMapping) {
        formClass.addElement(fieldMapping.getFormField());
        mappings.add(fieldMapping);
    }
    
    public void addTextField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, Mapping.TEXT));
    }

    public void addDateField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, Mapping.DATE));
    }

    public void addReferenceField(FormField field, final char domain, String columnName) {
        add(new FieldMapping(field, columnName, new ForeignKeyMapping(domain)));
    }
    
    public void addGeoAreaField(FormField field) {
        add(new FieldMapping(field, Arrays.asList("y1", "y1", "x1", "x2"), new FieldValueMapping() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {
                double minLat = rs.getDouble(index + 2);
                if (rs.wasNull()) {
                    return null;
                }
                double maxLat = rs.getDouble(index + 3);
                if (rs.wasNull()) {
                    return null;
                }
                double minLon = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }
                double maxLon = rs.getDouble(index + 1);
                if (rs.wasNull()) {
                    return null;
                }

                return new GeoArea(new Extents(minLat, maxLat, minLon, maxLon), "FIXME");
            }

            @Override
            public Collection<? extends Object> toParameters(FieldValue value) {
                throw new UnsupportedOperationException();
            }
        }));
    }

    public void addGeoPoint(FormField field) {
        add(new FieldMapping(field, Arrays.asList("x1", "y1"), new FieldValueMapping() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {

                double lat = rs.getDouble(index + 1);
                if (rs.wasNull()) {
                    return null;
                }

                double lon = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }

                return new GeoPoint(lat, lon);
            }

            @Override
            public Collection<Double> toParameters(FieldValue value) {
                GeoPoint pointValue = (GeoPoint) value;
                return Arrays.asList(pointValue.getLongitude(), pointValue.getLatitude());
            }
        }));
    }


    public TableMapping build() {
        Preconditions.checkState(primaryKeyMapping != null, tableName + ": Primary key is not set");
        Preconditions.checkState(formClass != null, tableName + ": FormClass is not set");
        Preconditions.checkState(formClass.getOwnerId() != null, tableName + ": ownerId is not set");
        return new TableMapping(tableName, tableName + " base", baseFilter, primaryKeyMapping, mappings, formClass);
    }
}
