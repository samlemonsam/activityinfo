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
import java.util.*;


public class TableMappingBuilder {
    private String tableName;
    private String fromClause;
    private String baseFilter;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;
    private FormClass formClass;
    private DeleteMethod deleteMethod = DeleteMethod.SOFT_BY_DATE;
    
    private Set<ResourceId> fieldIds = new HashSet<>();
    
    private Map<String, Object> insertDefaults = new HashMap<>();
    private long version = 0L;

    private TableMappingBuilder(ResourceId formClassId, String tableName) {
        this.tableName = tableName;
        this.fromClause = tableName + " base";
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

    public void setDatabaseId(ResourceId rootId) {
        formClass.setDatabaseId(rootId);
    }
    
    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }
    
    public void setBaseFilter(String baseFilter) {
        this.baseFilter = baseFilter;
    }

    public void add(FieldMapping fieldMapping) {
        if(fieldIds.contains(fieldMapping.getFieldId())) {
            throw new IllegalArgumentException("Duplicate field id " + fieldMapping.getFieldId());
        }
        fieldIds.add(fieldMapping.getFieldId());
        formClass.addElement(fieldMapping.getFormField());
        mappings.add(fieldMapping);
    }
    
    public void addUnmappedField(FormField field) {
        if(fieldIds.contains(field.getId())) {
            throw new IllegalArgumentException("Duplicate field id " + field.getId());
        }
        fieldIds.add(field.getId());
        formClass.addElement(field);
    }
    
    public void addTextField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, TextConverter.INSTANCE));
    }

    public void addDateField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, DateConverter.INSTANCE));
    }

    public void addReferenceField(FormField field, ResourceId formId,  final char domain, String columnName) {
        add(new FieldMapping(field, columnName, new ReferenceConverter(formId, domain)));
    }
    
    public void addGeoAreaField(FormField field) {
        add(new FieldMapping(field, Arrays.asList("x1", "y1", "x2", "y2"), new FieldValueConverter() {
            @Override
            public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {
                double x1 = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }
                double y1 = rs.getDouble(index + 1);
                if (rs.wasNull()) {
                    return null;
                }
                double x2 = rs.getDouble(index + 2);
                if (rs.wasNull()) {
                    return null;
                }
                double y2 = rs.getDouble(index + 3);
                if (rs.wasNull()) {
                    return null;
                }

                return new GeoArea(Extents.create(x1, y1, x2, y2), "FIXME");
            }

            @Override
            public Collection<? extends Object> toParameters(FieldValue value) {
                GeoArea area = (GeoArea) value;
                Extents bbox = area.getEnvelope();
                return Arrays.asList(bbox.getX1(), bbox.getY1(), bbox.getX2(), bbox.getY2());
            }
        }));
    }

    public void addGeoPoint(FormField field) {
        add(new FieldMapping(field, Arrays.asList("x", "y"), new FieldValueConverter() {
            @Override
            public FieldValue toFieldValue(ResultSet rs, int index) throws SQLException {

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
    
    public void defaultValueOnInsert(String fieldName, Object sqlValue) {
        insertDefaults.put(fieldName, sqlValue);
    }
    
    public void setDeleteMethod(DeleteMethod deleteMethod) {
        this.deleteMethod = Preconditions.checkNotNull(deleteMethod);
    }

    public void setSchemaVersion(long schemaVersion) {
        formClass.setSchemaVersion(schemaVersion);
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public TableMapping build() {
        Preconditions.checkState(formClass != null, fromClause + ": FormClass is not set");
        Preconditions.checkState(formClass.getLabel() != null, fromClause + ": formClass.label is null");
        Preconditions.checkState(primaryKeyMapping != null, fromClause + ": Primary key is not set");
        return new TableMapping(tableName, fromClause, baseFilter, primaryKeyMapping, mappings, formClass,
                deleteMethod, insertDefaults, version);
    }

    
}
