package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordKey;
import org.activityinfo.store.hrd.entity.FormSchemaKey;
import org.activityinfo.store.query.impl.InvalidUpdateException;

import java.util.Map;


public class CreateOrUpdateRecord implements Operation {

    private ResourceId collectionId;
    private RecordUpdate update;

    public CreateOrUpdateRecord(ResourceId collectionId, RecordUpdate update) {
        this.collectionId = collectionId;
        this.update = update;
    }

    @Override
    public void execute(Datastore datastore) throws EntityNotFoundException {

        FormClass formClass = datastore.load(new FormSchemaKey(collectionId)).readFormClass();
        FormRecordKey key = new FormRecordKey(update.getResourceId());
        if(!key.getCollectionId().equals(collectionId)) {
            throw new IllegalStateException();
        }

        Optional<FormRecordEntity> existingEntity = datastore.loadIfPresent(key);
        FormRecordEntity updated;
        
        if(existingEntity.isPresent()) {
            updated = existingEntity.get();
            
        } else {
            updated = new FormRecordEntity(key);

            if (formClass.getParentFormId().isPresent()) {
                ResourceId parentId = update.getParentId();
                if (parentId == null) {
                    throw new InvalidUpdateException("@parent is required for subform submissions");
                }
                if (update.getKeyId() == null) {
                    throw new InvalidUpdateException("@key is required for subform submissions");
                }
                updated.setParentId(parentId);
                updated.setKeyId(update.getKeyId());
            }
        }

        for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
            // workaround for current UI
            if(entry.getKey().equals(ResourceId.valueOf("keyId"))) {
                TextValue keyText = (TextValue)entry.getValue(); 
                updated.setProperty(entry.getKey().asString(), keyText.asString());
                
            } else {
                FormField field = formClass.getField(entry.getKey());
                FieldConverter converter = FieldConverters.forType(field.getType());
                if (entry.getValue() != null) {
                    updated.setProperty(field.getName(), converter.toHrdProperty(entry.getValue()));
                }
            }
        }
        
        datastore.put(updated);
    }
}
