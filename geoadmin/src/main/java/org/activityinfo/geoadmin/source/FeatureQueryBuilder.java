package org.activityinfo.geoadmin.source;

import com.google.common.base.Function;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        QueryField field = new QueryField();
        field.attributeIndex = findIndex(fieldId);
        field.observer = observer;
        field.converter = new Function<Object, FieldValue>() {
            @Override
            public FieldValue apply(Object input) {
                assert input instanceof String;
                return TextValue.valueOf((String)input);
            }
        };
        fields.add(field);
    }

    private int findIndex(ResourceId fieldId) {
        int index = 0;
        for (AttributeDescriptor attribute : featureSource.getSchema().getAttributeDescriptors()) {
            if(attribute.getName().getURI().equals(fieldId.asString())) {
                return index;
            }
            index++;
        }
        throw new IllegalArgumentException(fieldId.asString());
    }

    @Override
    public void execute() throws IOException {
        SimpleFeatureIterator it = featureSource.getFeatures().features();
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
