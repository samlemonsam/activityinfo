package org.activityinfo.geoadmin.source;

import com.google.common.base.Function;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.geoadmin.source.FeatureSourceCollection.FIELD_ID_PREFIX;


public class FeatureQueryBuilder implements ColumnQueryBuilder {

    private static class QueryField {
        private int attributeIndex;
        private CursorObserver<FieldValue> observer;
        private Function<Object, FieldValue> converter;
    }
    
    
    private final SimpleFeatureSource featureSource;
    private final List<QueryField> fields = new ArrayList<>();
    private final List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();

    public FeatureQueryBuilder(SimpleFeatureSource featureSource) {
        this.featureSource = featureSource;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        QueryField field = new QueryField();
        field.attributeIndex = findIndex(fieldId);
        field.observer = observer;
        field.converter = converterForAttribute(field.attributeIndex);
        fields.add(field);
    }

    private Function<Object, FieldValue> converterForAttribute(int attributeIndex) {
        AttributeDescriptor descriptor = featureSource.getSchema().getAttributeDescriptors().get(attributeIndex);
        AttributeType type = descriptor.getType();
        if(type instanceof GeometryType) {
            return new GeometryConverter((GeometryType)type);
            
        } else {
            return new StringAttributeConverter();
        }
      
    }

    private int findIndex(ResourceId fieldId) {
        try {
            return Integer.parseInt(fieldId.asString().substring(FIELD_ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid field id: '" + fieldId + "'");
        }
    }

    
    @Override
    public void execute() {
        SimpleFeatureIterator it;
        try {
            it = featureSource.getFeatures().features();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(it.hasNext()) {
            SimpleFeature feature = it.next();
            ResourceId id = ResourceId.valueOf(feature.getID());
            for(CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(id);
            }
            for(QueryField field : fields) {
                Object value = feature.getAttribute(field.attributeIndex);
                if(value == null) {
                    field.observer.onNext(null);
                } else {
                    field.observer.onNext(field.converter.apply(value));
                }
            }
        }
        for (CursorObserver<ResourceId> idObserver : idObservers) {
            idObserver.done();
        }
        for(QueryField field : fields) {
            field.observer.done();
        }
    }
}
