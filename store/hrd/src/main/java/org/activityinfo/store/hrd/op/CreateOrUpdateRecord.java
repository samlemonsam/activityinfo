package org.activityinfo.store.hrd.op;

import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.RecordChangeType;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.query.impl.InvalidUpdateException;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class CreateOrUpdateRecord extends VoidWork {

    private ResourceId formId;
    private RecordUpdate update;

    public CreateOrUpdateRecord(ResourceId formId, RecordUpdate update) {
        this.formId = formId;
        this.update = update;
    }


    @Override
    public void vrun() {

        FormEntity rootEntity = ofy().load().key(FormEntity.key(formId)).safe();
        FormClass formClass = ofy().load().key(FormSchemaEntity.key(formId)).safe().readFormClass();

        long currentVersion = rootEntity.getVersion();
        long newVersion = currentVersion + 1;

        rootEntity.setVersion(newVersion);

        FormRecordEntity existingEntity = ofy().load().key(FormRecordEntity.key(formClass, update.getRecordId())).now();
        FormRecordEntity updated;
        RecordChangeType changeType;
        
        if(existingEntity != null) {
            updated = existingEntity;
            changeType = RecordChangeType.UPDATED;
            
        } else {
            updated = new FormRecordEntity(formId, update.getRecordId());
            changeType = RecordChangeType.CREATED;

            if (formClass.getParentFormId().isPresent()) {
                ResourceId parentId = update.getParentId();
                if (parentId == null) {
                    throw new InvalidUpdateException("@parent is required for subform submissions");
                }
                updated.setParentRecordId(parentId);
            }
        }
        updated.setVersion(newVersion);
        updated.setSchemaVersion(rootEntity.getSchemaVersion());
        updated.setDeleted(update.isDeleted());
        updated.setFieldValues(formClass, update.getChangedFieldValues());
        
        // Store a copy as a snapshot
        FormRecordSnapshotEntity snapshotEntity = new FormRecordSnapshotEntity(update.getUserId(), changeType, updated);
        
        ofy().save().entities(rootEntity, updated, snapshotEntity);
    }

}
