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
package org.activityinfo.geoadmin.source;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.List;


public class FeatureSourceStorage implements FormStorage {

    public static final String FIELD_ID_PREFIX = "ATTRIB";
    
    private final ResourceId resourceId;
    private final SimpleFeatureSource featureSource;

    public FeatureSourceStorage(ResourceId resourceId, SimpleFeatureSource featureSource) {
        this.resourceId = resourceId;
        this.featureSource = featureSource;
    }


    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureSource getFeatureSource() {
        return featureSource;
    }

    @Override
    public FormClass getFormClass() {
        FormClass formClass = new FormClass(resourceId);
        formClass.setLabel(featureSource.getName().getLocalPart());
        List<AttributeDescriptor> attributes = featureSource.getSchema().getAttributeDescriptors();
        for (int i = 0; i < attributes.size(); i++) {
            AttributeDescriptor attribute = attributes.get(i);
            formClass.addElement(toField(i, attribute));
        }
        return formClass;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(TypedRecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(TypedRecordUpdate update) {
        throw new UnsupportedOperationException();
    }


    private FormField toField(int i, PropertyDescriptor descriptor) {
        FormField field = new FormField(ResourceId.valueOf(FIELD_ID_PREFIX + i));
        field.setLabel(descriptor.getName().getLocalPart());
        field.setCode(descriptor.getName().getLocalPart());
        field.setType(AttributeTypeAdapters.of(descriptor).createType());
        return field;
    }

    public int getGeometryAttributeIndex() {
        List<AttributeDescriptor> attributeDescriptors = featureSource.getSchema().getAttributeDescriptors();
        for (int i = 0; i < attributeDescriptors.size(); i++) {
            AttributeDescriptor descriptor = attributeDescriptors.get(i);
            if (descriptor.getType() instanceof GeometryType) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new FeatureQueryBuilder(featureSource);
    }

    @Override
    public long cacheVersion() {
        return 0;
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry value) {
        throw new UnsupportedOperationException();
    }


}
