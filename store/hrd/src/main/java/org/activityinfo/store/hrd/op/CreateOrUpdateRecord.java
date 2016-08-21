package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.RecordChangeType;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;
import org.activityinfo.store.hrd.entity.*;
import org.activityinfo.store.query.impl.InvalidUpdateException;

import java.util.Map;


public class CreateOrUpdateRecord implements Operation {

    private ResourceId formId;
    private RecordUpdate update;

    public CreateOrUpdateRecord(ResourceId formId, RecordUpdate update) {
        this.formId = formId;
        this.update = update;
    }

    @Override
    public void execute(Datastore datastore) throws EntityNotFoundException {

        FormClass formClass = datastore.load(new FormSchemaKey(formId)).readFormClass();
        FormRecordKey key = new FormRecordKey(update.getRecordId());
        if(!key.getCollectionId().equals(formId)) {
            throw new IllegalStateException();
        }

        FormVersionEntity versionEntity = datastore.load(new FormVersionKey(formClass.getId()));
        long currentVersion = versionEntity.getVersion();
        long newVersion = currentVersion + 1;

        versionEntity.setVersion(newVersion);
        
        Optional<FormRecordEntity> existingEntity = datastore.loadIfPresent(key);
        FormRecordEntity updated;
        RecordChangeType changeType;
        
        if(existingEntity.isPresent()) {
            updated = existingEntity.get();
            changeType = RecordChangeType.UPDATED;
            
        } else {
            updated = new FormRecordEntity(key);
            changeType = RecordChangeType.CREATED;

            if (formClass.getParentFormId().isPresent()) {
                ResourceId parentId = update.getParentId();
                if (parentId == null) {
                    throw new InvalidUpdateException("@parent is required for subform submissions");
                }
                updated.setParentId(parentId);
            }
        }
        updated.setVersion(newVersion);
        updated.setSchemaVersion(versionEntity.getSchemaVersion());
        updated.setDeleted(update.isDeleted());

        for (Map.Entry<ResourceId, FieldValue> entry : update.getChangedFieldValues().entrySet()) {
            // workaround for current UI
            if(entry.getKey().equals(ResourceId.valueOf("keyId"))) {
                TextValue keyText = (TextValue)entry.getValue(); 
                updated.setFieldValue(entry.getKey().asString(), keyText.asString());
                
            } else {
                FormField field = formClass.getField(entry.getKey());
                FieldConverter converter = FieldConverters.forType(field.getType());
                if (entry.getValue() != null) {
                    updated.setFieldValue(field.getName(), converter.toHrdProperty(entry.getValue()));
                }
            }
        }
        
        // Store a copy as a snapshot
        FormRecordSnapshotEntity snapshotEntity = new FormRecordSnapshotEntity(update.getUserId(), changeType, updated);
        
        datastore.put(updated, versionEntity, snapshotEntity);
    }
}
