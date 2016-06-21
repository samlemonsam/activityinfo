package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.service.store.ResourceCollection;

import java.io.IOException;
import java.util.Map;

/**
 * Fetches a single form record using the ColumnSetBuilder
 */
public class ResourceFetcher {
    
    private ResourceCollection collection;

    private class CollectingObserver<T> implements CursorObserver<T> {

        private T value;
        
        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void done() {

        }
    }
    
    public ResourceFetcher(ResourceCollection collection) {
        this.collection = collection;
    }

    public Optional<Resource> get(ResourceId resourceId) throws IOException {
        FormClass formClass = collection.getFormClass();

        CollectingObserver<ResourceId> id = new CollectingObserver<>();
        Map<ResourceId, CollectingObserver<FieldValue>> fields = Maps.newHashMap();

        ColumnQueryBuilder builder = collection.newColumnQuery();
        builder.addResourceId(id);
        builder.only(resourceId);

        for (FormField formField : formClass.getFields()) {
            CollectingObserver<FieldValue> fieldValue = new CollectingObserver<>();
            builder.addField(formField.getId(), fieldValue);
            fields.put(formField.getId(), fieldValue);
        }

        builder.execute();

        if (id.value == null) {
            return Optional.absent();

        } else {

            Resource resource = Resources.createResource();
            resource.setId(resourceId);
            resource.setOwnerId(formClass.getId());
            resource.set("classId", formClass.getId().asString());

            for (FormField formField : formClass.getFields()) {
                resource.set(formField.getId(), fields.get(formField.getId()).value);
            }
            return Optional.of(resource);

        }
    }
}
