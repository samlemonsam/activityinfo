package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;
import org.activityinfo.store.hrd.entity.Datastore;
import org.activityinfo.store.hrd.entity.FormClassKey;
import org.activityinfo.store.hrd.entity.FormSubmission;
import org.activityinfo.store.hrd.entity.FormSubmissionKey;
import org.activityinfo.store.query.impl.InvalidUpdateException;

import java.util.Map;


public class CreateOrUpdateSubmission implements Operation {

    private ResourceId collectionId;
    private ResourceUpdate update;

    public CreateOrUpdateSubmission(ResourceId collectionId, ResourceUpdate update) {
        this.collectionId = collectionId;
        this.update = update;
    }

    @Override
    public void execute(Datastore datastore) throws EntityNotFoundException {

        FormClass formClass = datastore.load(new FormClassKey(collectionId)).readFormClass();
        FormSubmissionKey key = new FormSubmissionKey(update.getResourceId());
        if(!key.getCollectionId().equals(collectionId)) {
            throw new IllegalStateException();
        }

        Optional<FormSubmission> existingEntity = datastore.loadIfPresent(key);
        FormSubmission updated;
        
        if(existingEntity.isPresent()) {
            updated = existingEntity.get();
            
        } else {
            updated = new FormSubmission(key);

            if (formClass.getParentFormId().isPresent()) {
                ResourceId parentId = update.getParentId();
                if (parentId == null) {
                    throw new InvalidUpdateException("@parent is required for subform submissions");
                }
                updated.setParentId(parentId);
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
